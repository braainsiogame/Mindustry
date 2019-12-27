package io.anuke.mindustry.world.blocks.logic.commanderblock;

import io.anuke.arc.collection.Array;
import io.anuke.arc.collection.IntArray;
import io.anuke.arc.func.Func2;
import io.anuke.arc.graphics.g2d.BitmapFont;
import io.anuke.arc.input.KeyCode;
import io.anuke.arc.scene.Element;
import io.anuke.arc.scene.event.InputEvent;
import io.anuke.arc.scene.ui.TextArea;
import io.anuke.arc.scene.utils.Disableable;

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
        for(int i = firstLineShowing * 2; i < (firstLineShowing + linesShowing) * 2 && i < linesBreak.size; i += 2) {
            for(int j = 0; j < displayText.length(); j++){
                font.draw(displayText.subSequence(j, j + 1), x + j, y + offsetY, linesBreak.items[i], j, 0.0F, 8, false);
            }
            offsetY -= font.getLineHeight();
        }
    }*/
}
