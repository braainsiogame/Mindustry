package io.anuke.mindustry.world.blocks.logic;

import io.anuke.annotations.Annotations.*;
import io.anuke.arc.*;
import io.anuke.arc.Input.*;
import io.anuke.arc.graphics.*;
import io.anuke.arc.graphics.g2d.*;
import io.anuke.arc.math.Mathf;
import io.anuke.arc.math.geom.*;
import io.anuke.arc.scene.ui.*;
import io.anuke.arc.scene.ui.layout.*;
import io.anuke.arc.util.*;
import io.anuke.arc.util.pooling.*;
import io.anuke.mindustry.entities.*;
import io.anuke.mindustry.entities.effect.Fire;
import io.anuke.mindustry.entities.type.*;
import io.anuke.mindustry.gen.*;
import io.anuke.mindustry.net.*;
import io.anuke.mindustry.ui.*;
import io.anuke.mindustry.ui.dialogs.*;
import io.anuke.mindustry.world.*;
import io.anuke.mindustry.world.blocks.logic.commanderblock.BoxedValue;
import io.anuke.mindustry.world.blocks.logic.commanderblock.Instruction;
import io.anuke.mindustry.world.blocks.logic.commanderblock.Parser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import static io.anuke.mindustry.Vars.*;

public class DroneCommanderBlock extends Block{
    protected static int maxTextLength = 22000;
    protected static int maxNewlines = 2400;

    public DroneCommanderBlock(String name){
        super(name);
        update = true;
        configurable = true;
        solid = true;
        destructible = true;
        entityType = DroneCommanderBlockEntity::new;
    }

    @Remote(targets = Loc.both, called = Loc.both, forward = true)
    public static void setDroneCommanderBlockText(Player player, Tile tile, String text){
        if(!Units.canInteract(player, tile)) return;
        if(net.server() && text.length() > maxTextLength){
            throw new ValidateException(player, "Player has gone above text limit.");
        }

        //can be broken while a player is typing
        if(!(tile.block() instanceof DroneCommanderBlock)){
            return;
        }

        StringBuilder result = new StringBuilder(text.length());
        text = text.trim();
        int count = 0;
        for(int i = 0; i < text.length(); i++){
            char c = text.charAt(i);
            if(c == '\n' || c == '\r'){
                count ++;
                if(count <= maxNewlines){
                    result.append('\n');
                }
            }else{
                result.append(c);
            }
        }

        DroneCommanderBlockEntity entity = tile.ent();
        if(entity != null){
            entity.setMessage(result.toString());
        }
    }

    @Override
    public void drawSelect(Tile tile){
        DroneCommanderBlockEntity entity = tile.ent();
        BitmapFont font = Fonts.outline;
        GlyphLayout l = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        boolean ints = font.usesIntegerPositions();
        font.getData().setScale(1 / 4f / Scl.scl(1f));
        font.setUseIntegerPositions(false);

        String text = entity.getPreviewMessage() == null || entity.getPreviewMessage().isEmpty() ? "[lightgray]" + Core.bundle.get("empty") : entity.getPreviewMessage();

        l.setText(font, text, Color.white, 90f, Align.left, true);
        float offset = 1f;

        Draw.color(0f, 0f, 0f, 0.2f);
        Fill.rect(tile.drawx(), tile.drawy() - tilesize/2f - l.height/2f - offset, l.width + offset*2f, l.height + offset*2f);
        Draw.color();
        font.setColor(Color.white);
        font.draw(text, tile.drawx() - l.width/2f, tile.drawy() - tilesize/2f - offset, 90f, Align.left, true);
        font.setUseIntegerPositions(ints);

        font.getData().setScale(1f);

        Pools.free(l);
    }

    @Override
    public void buildConfiguration(Tile tile, Table table){
        DroneCommanderBlockEntity entity = tile.ent();

        table.addImageButton(Icon.pencilSmall, () -> {
            if(mobile){
                Core.input.getTextInput(new TextInput(){{
                    text = entity.getMessage();
                    multiline = true;
                    maxLength = maxTextLength;
                    accepted = out -> {
                        Call.setDroneCommanderBlockText(player, tile, out);
                    };
                }});
            }else{
                FloatingDialog dialog = new FloatingDialog("$editmessage");
                dialog.setFillParent(false);
                TextArea a = dialog.cont.add(new TextArea(entity.getMessage().replace("\n", "\r"))).size(380f, 160f).get();
                a.setFilter((textField, c) -> {
                    if(c == '\n' || c == '\r'){
                        int count = 0;
                        for(int i = 0; i < textField.getText().length(); i++){
                            if(textField.getText().charAt(i) == '\n' || textField.getText().charAt(i) == '\r'){
                                count++;
                            }
                        }
                        return count < maxNewlines;
                    }
                    return true;
                });
                a.setMaxLength(maxTextLength);
                dialog.buttons.addButton("$ok", () -> {
                    Call.setDroneCommanderBlockText(player, tile, a.getText());
                    dialog.hide();
                }).size(130f, 60f);
                dialog.update(() -> {
                    if(!entity.isValid()){
                        dialog.hide();
                    }
                });
                dialog.show();
            }
            control.input.frag.config.hideConfig();
        }).size(40f);
    }

    @Override
    public void updateTableAlign(Tile tile, Table table){
        Vector2 pos = Core.input.mouseScreen(tile.drawx(), tile.drawy() + tile.block().size * tilesize / 2f + 1);
        table.setPosition(pos.x, pos.y, Align.bottom);
    }

    public static class DroneCommanderBlockEntity extends TileEntity{
        private String message;
        private Instruction[] instructions;
        public final int maxInstructionsPerStep = 200;
        public int sleepCycles = 0;
        public HashMap<Object, BoxedValue> variables;
        private String errorMsg = null;
        private boolean isEmpty;
        public HashMap<Object, Integer> labels;
        public int[] stack = new int[1024];
        public int stackIndex = 0;
        public DroneCommanderBlockEntity(){
            super();
            setMessage("sleep 1\n");
        }
        @Override
        public void update() {
            super.update();
            if(errorMsg != null){
                return;
            }
            int steps = Mathf.round(delta());
            while(steps-- > 0){
                if(sleepCycles > 0){
                    --sleepCycles;
                    continue;
                }
                int instructionsRun = 0;
                while(instructionsRun++ < maxInstructionsPerStep && sleepCycles == 0){
                    if(isEmpty){
                        instructionsRun = maxInstructionsPerStep + 1;
                        break;
                    }
                    if(stack[stackIndex] >= instructions.length){
                        stack[stackIndex] = 0;
                    }
                    Instruction instruction = instructions[stack[stackIndex]];
                    if(instruction == null || (instruction.initOnly && instruction.alreadyRun)){
                        --instructionsRun;
                    } else {
                        try {
                            instruction.run(this);
                        } catch(Instruction.InstructionError e){
                            handleError(e, stack[stackIndex] + 1, 0);
                        }
                    }
                    stack[stackIndex]++;
                }
                if(instructionsRun > maxInstructionsPerStep){
                    Fire.create(tile);
                    break;
                }
            }
        }

        @Override
        public void write(DataOutput stream) throws IOException{
            super.write(stream);
            stream.writeUTF(getMessage());
        }

        @Override
        public void read(DataInput stream, byte revision) throws IOException{
            super.read(stream, revision);
            setMessage(stream.readUTF());
        }

        public String getMessage() {
            return message;
        }

        public String getPreviewMessage() {
            return errorMsg == null ? message : errorMsg;
        }

        public void setMessage(String message) {
            Log.info("Recompiling. . .");
            this.message = message;
            stack[0] = 0;
            stackIndex = 0;
            errorMsg = null;
            String[] lines = message.split("\n");
            variables = new HashMap<>();
            instructions = new Instruction[lines.length];
            labels = new HashMap<>();
            isEmpty = true;
            sleepCycles = 0;
            for(int i = 0; i < lines.length; i++){
                Log.info(lines[i]);
                Parser.CharStream charStream = new Parser.CharStream(lines[i]);
                try {
                    if(!charStream.finished() && charStream.peek() != '#'){
                        if(charStream.peek() == ':'){
                            charStream.next();
                            BoxedValue key = Parser.parseAny(charStream);
                            labels.put(key.resolve(this), i);
                        } else {
                            Instruction instruction = Instruction.parse(charStream);
                            instructions[i] = instruction;
                            if(!instruction.initOnly){
                                isEmpty = false;
                            }
                        }
                    }
                } catch(Parser.InstructionSyntaxError e){
                    handleError(e, i + 1, charStream.index() + 1);
                    isEmpty = true;
                    break;
                }
            }
        }
        private void handleError(Error e, int line, int chr){
            StringBuilder errorMsgBuilder = new StringBuilder("[red]");
            errorMsgBuilder.append(e.getClass().getSimpleName());
            errorMsgBuilder.append(": [scarlet]");
            errorMsgBuilder.append(e.getMessage());
            errorMsgBuilder.append(" [lightgray](line ");
            errorMsgBuilder.append(line);
            if(chr > 0){
                errorMsgBuilder.append(" char ");
                errorMsgBuilder.append(chr);
            }
            errorMsgBuilder.append(")");
            errorMsg = errorMsgBuilder.toString();
        }
    }
}
