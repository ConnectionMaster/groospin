package org.hs5tb.groospin.checker.result
/**
 * Created by Alberto on 12-Jun-16.
 */
class CheckResult {

    int roms = 0
    int exes = 0
    int wheels = 0
    int videos = 0
    int themes = 0
    int artwork1 = 0
    int artwork2 = 0
    int artwork3 = 0
    int artwork4 = 0

/*
    String toString() {
        getLongInfo(", ")
    }

    String getQuickInfo(String delimiter) {
        ["\"${systemName}${romName ? ":${romName}" : ""}\"", totalRoms, roms, wheels, videos].join(delimiter)
    }

    String getShortInfo(String delimiter) {
        ["\"${systemName}${romName ? ":${romName}" : ""}\"", totalRomSize, "\"${IOTools.humanReadableByteCount(totalRomSize)}\"", totalRoms, roms, exes, works, wheels, videos].join(delimiter)
    }

    String getLongInfo(String delimiter) {
        getShortInfo(delimiter) + delimiter + [themes, artwork1, artwork2, artwork3, artwork4].join(delimiter)
    }

*/
    void add(CheckResult checkResult) {
        roms += checkResult.roms
        exes += checkResult.exes
        wheels += checkResult.wheels
        videos += checkResult.videos
        themes += checkResult.themes
        artwork1 += checkResult.artwork1
        artwork2 += checkResult.artwork2
        artwork3 += checkResult.artwork3
        artwork4 += checkResult.artwork4
    }
}
