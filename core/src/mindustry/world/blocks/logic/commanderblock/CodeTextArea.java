package mindustry.world.blocks.logic.commanderblock;

import arc.struct.Array;
import arc.struct.IntArray;
import arc.func.Func2;
import arc.graphics.g2d.BitmapFont;
import arc.input.KeyCode;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.ui.TextArea;
import arc.scene.utils.Disableable;

import java.lang.reflect.Field;

public class CodeTextArea extends TextArea {
    private static InputEvent spaceKeyEvent;
    private static Field field;
    private IntArray linesBreak;
    static {
        spaceKeyEvent = new InputEvent();
        spaceKeyEvent.type = InputEvent.Type.keyTyped;
        spaceKeyEvent.keyCode = KeyCode.SPACE;
        spaceKeyEvent.character = ' ';
        try {
            field = TextArea.class.getDeclaredField("linesBreak");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
    public CodeTextArea(String text) {
        super(text);
        keyDown(KeyCode.TAB, () -> {
            fire(spaceKeyEvent);
            fire(spaceKeyEvent);
            fire(spaceKeyEvent);
            fire(spaceKeyEvent);
        });
        try {
            linesBreak = (IntArray) field.get(this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    /*
    @Override
    protected void drawText(BitmapFont font, float x, float y) {
        float offsetY = 0.0F;
        int firstLineShowing = getFirstLineShowing();
        int linesShowing = getLinesShowing();
        BitmapFont.BitmapFontData fontData = font.getData();
        for(int i = firstLineShowing * 2; i < (firstLineShowing + linesShowing) * 2 && i < linesBreak.size; i += 2) {
            for(int j = linesBreak.items[i]; j < linesBreak.items[i + 1]; j++){
                char c = displayText.charAt(j);
                float calcX = (j - linesBreak.items[i]) * fontData.getGlyph(c).width + x + 1;
                font.draw(Character.toString(c), calcX, y + offsetY);
            }
            //font.draw(displayText, x, y + offsetY, linesBreak.items[i], linesBreak.items[i + 1], 0.0F, 8, false);
            offsetY -= font.getLineHeight();
        }
    }*/
}
