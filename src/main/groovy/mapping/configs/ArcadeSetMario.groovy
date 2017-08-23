package mapping.configs

import org.hs5tb.groospin.base.J2K

/**
 * Created by Alberto on 21-Aug-17.
 */
class ArcadeSetMario extends ArcadeSet {
    J2K.Preset preset
    int player1 = 1
    int player2 = 2

    void p1Action1(key) { preset.buttonToKey(player1, 1, key) }

    void p1Action2(key) { preset.buttonToKey(player1, 2, key) }

    void p1Action3(key) { preset.buttonToKey(player1, 3, key) }

    void p1Action4(key) { preset.buttonToKey(player1, 4, key) }

    void p1Action5(key) { preset.buttonToKey(player1, 5, key) }

    void p1Action6(key) { preset.buttonToKey(player1, 6, key) }

    void p1Start(key) { preset.buttonToKey(player1, 8, key) }

    void exit(key) { preset.buttonToKey(player1, 10, key) }

    void pinballLeft(key) { preset.buttonToKey(player1, 9, key) }

    void pinballRight(key) { preset.buttonToKey(player2, 9, key) }

    void p2Action1(key) { preset.buttonToKey(player2, 1, key) }

    void p2Action2(key) { preset.buttonToKey(player2, 2, key) }

    void p2Action3(key) { preset.buttonToKey(player2, 3, key) }

    void p2Action4(key) { preset.buttonToKey(player2, 4, key) }

    void p2Action5(key) { preset.buttonToKey(player2, 5, key) }

    void p2Action6(key) { preset.buttonToKey(player2, 6, key) }

    void p2Start(key) { preset.buttonToKey(player2, 8, key) }

    void coin(key) {
        preset.buttonToKey(player1, 7, key)
        preset.buttonToKey(player2, 7, key)
    }

}
