package com.github.epd.sprout;

import com.watabou.noosa.Game;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class UndoManager {

    private static final int MAX_HISTORY = 200;

    private static final Deque<Integer> history = new ArrayDeque<>();
    private static int nextSlot = 0;

    private UndoManager() {}

    public static void snapshot() {
        if (Dungeon.hero == null || !Dungeon.hero.isAlive() || Dungeon.level == null) {
            return;
        }

        int slot = nextSlot++;
        try {
            Dungeon.saveGame(slotGameFile(slot));
            Dungeon.saveLevel(slotLevelFile(slot), Dungeon.depth);

            history.addLast(slot);
            while (history.size() > MAX_HISTORY) {
                int oldest = history.removeFirst();
                deleteSlot(oldest);
            }
        } catch (IOException e) {
            deleteSlot(slot);
        }
    }

    public static boolean canUndo() {
        return !history.isEmpty();
    }

    public static int popForUndo() {
        if (history.isEmpty()) {
            return -1;
        }
        return history.removeLast();
    }

    public static void discardSlot(int slot) {
        deleteSlot(slot);
    }

    public static void clear() {
        for (int slot : history) {
            deleteSlot(slot);
        }
        history.clear();
        nextSlot = 0;
    }

    private static void deleteSlot(int slot) {
        Game.instance.deleteFile(slotGameFile(slot));
        Game.instance.deleteFile(slotLevelFile(slot));
    }

    public static String slotGameFile(int slot) {
        return "undo_game_" + slot + ".dat";
    }

    public static String slotLevelFile(int slot) {
        return "undo_level_" + slot + ".dat";
    }
}
