package examples

import org.hs5tb.groospin.base.*
import org.hs5tb.groospin.checker.*
import org.hs5tb.groospin.checker.handlers.*

HyperSpin hs = new HyperSpin(
        "D:/Games/Hyperspin-fe",
        "D:/Games/RocketLauncher")

def systems = ["MAME", "HBMAME"]
/*
def systems = ["HBMAME", "MAME", "MAME 4 Players",
                      "Namco Classics",
                      "Atari Classics", "Capcom Classics", "Cave", "Data East Classics",
                      "Banpresto", "Kaneko", "Irem Classics", "Williams Classics", "Midway Classics",
                      "Sega Classics", "Konami Classics", "Taito Classics", "SNK Classics"].sort()
*/

new Checker(hs).
        addHandler(new HumanInfo(false)).
        addHandler(new PrintMissing()).
        checkSystems(systems)


