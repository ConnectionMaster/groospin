package org.hs5tb.groospin.checker.handlers

import org.hs5tb.groospin.checker.BaseCheckHandler
import org.hs5tb.groospin.checker.result.CheckTotalResult
import static org.hs5tb.groospin.common.IOTools.humanReadableByteSize

/**
 * Created by Alberto on 12-Jun-16.
 */
class HumanInfo extends BaseCheckHandler {

    Boolean folderSize

    HumanInfo() {}

    HumanInfo(boolean folderSize) {
        this.folderSize = folderSize
    }

    @Override
    void startGroup(String groupName) {
        println "${groupName.center(143, " ")}"
    }

    long start
    @Override
    void startCheck() {
        start = System.currentTimeMillis()
    }

    @Override
    void endSystem(CheckTotalResult checkResult) {
        drawLine(checkResult.systemName, checkResult)
    }

    @Override
    void endGroup(CheckTotalResult checkResult) {
        println "-"*143
        drawLine("Total ${checkResult.group}", checkResult)
        println "-"*143
        println ""
    }

    @Override
    void endCheck(CheckTotalResult checkResult) {
        int totalSecs = (System.currentTimeMillis() - start) / 1000
        int hours = totalSecs / 3600
        int minutes = (totalSecs % 3600) / 60
        int seconds = totalSecs % 60

        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        drawLine("TOTAL", checkResult)
        println "Time: ${timeString}"
    }

    void drawLine(String title, CheckTotalResult checkResult) {
        println "${title.padRight(40, " ")} roms: ${checkResult.exes.toString().padRight(4," ")}/${checkResult.totalRoms.toString().padRight(4," ")} w/v/t: ${"${checkResult.wheels}/${checkResult.videos}/${checkResult.themes}".padRight(14," ")} artwork: ${"${checkResult.artwork1}/${checkResult.artwork2}/${checkResult.artwork3}/${checkResult.artwork4}".padRight(19," ")} - roms ${humanReadableByteSize(checkResult.totalRomSize).padLeft(8, " ")} - size ${humanReadableByteSize(checkResult.totalMediaSize).padLeft(8, " ")}"
    }

    @Override
    boolean needsRomFolderSize() {
        folderSize != null?folderSize:super.needsRomFolderSize()
    }

    @Override
    boolean needsMediaFolderSize() {
        folderSize != null?folderSize:super.needsMediaFolderSize()
    }
}
