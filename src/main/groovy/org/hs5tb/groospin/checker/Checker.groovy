/*
 * hs5tb
 *
 * @author Alberto Vilches (alberto.vilches@hs5tb)
 * @date 13/6/16
 * Copyright. All Rights Reserved.
 */
package org.hs5tb.groospin.checker

import org.hs5tb.groospin.base.HyperSpin
import org.hs5tb.groospin.base.RLSystem
import org.hs5tb.groospin.common.IOTools;

public class Checker {

    HyperSpin hyperSpin
    Report report

    Checker(HyperSpin hyperSpin) {
        this(hyperSpin, new Report())
    }

    Checker(HyperSpin hyperSpin, String reportRoot) {
        this(hyperSpin, new File(reportRoot))
    }

    Checker(HyperSpin hyperSpin, File reportRoot) {
        this(hyperSpin, new Report(reportRoot))
    }

    Checker(HyperSpin hyperSpin, Report report) {
        this.hyperSpin = hyperSpin
        this.report = report
    }

    CheckResult check(String systemName, String rom) {
        check(systemName, [rom])
    }

    CheckResult check(String systemName, List roms = null) {
        RLSystem system = hyperSpin.getSystem(systemName)
        // println romFilenames
        int totalSize = system.calculateRomPathSize()
        if (!roms) roms = hyperSpin.listRomNames(systemName)
        CheckResult checkResult = new CheckResult(systemName: system.name, games: roms.size(), totalSize: totalSize, game: roms.size() == 1 ? roms.first() : null)
        roms.sort().each { String game ->
            report.haveRom(game)
            File romFound = system.findValidRom(game)
            if (romFound) {
                checkResult.roms ++
                if (system.needsExecutable()) {
                    File exe = system.findExecutable(game, romFound)
                    if (exe) {
                        checkResult.exes ++
                        checkResult.works ++
                    } else {
                        report.warn "$systemName Missing executable: ${game}"
                    }
                } else {
                    checkResult.works ++
                }
            } else {
                report.warn "$systemName Missing rom: ${game}"
            }

            checkResult.wheels += existsInMedia("${systemName}/Images/Wheel/${game}", ["jpg", "png"]) ? 1 : 0
            checkResult.videos += existsInMedia("${systemName}/Video/${game}", ["mp4", "flv"]) ? 1 : 0
            checkResult.themes += existsInMedia("${systemName}/Themes/${game}", ["zip"]) ? 1 : 0
            checkResult.artwork1 += existsInMedia("${systemName}/Images/Artwork1/${game}", ["jpg", "png"]) ? 1 : 0
            checkResult.artwork2 += existsInMedia("${systemName}/Images/Artwork2/${game}", ["jpg", "png"]) ? 1 : 0
            checkResult.artwork3 += existsInMedia("${systemName}/Images/Artwork3/${game}", ["jpg", "png"]) ? 1 : 0
            checkResult.artwork4 += existsInMedia("${systemName}/Images/Artwork4/${game}", ["jpg", "png"]) ? 1 : 0
        }
        return checkResult
    }

    boolean existsInMedia(String path, List extensions) {
        return IOTools.findFileWithExtensions(new File(hyperSpin.hsRoot, "Media/${path}"), extensions)
    }

/*
    void listSystems(List systems) {
        log(["systemName","bytes","human size","db xml games","rom files","media:wheels","media:videos","media:themes","media:artwork1","media:artwork2","media:artwork3","media:artwork4"].join(delimiter))

        systems.each { String systemName ->
            haveSystem(systemName)
            CheckResult checkResult = checkAllGames(systemName)
            log(checkResult.getLongInfo(delimiter))
            endHave()
        }
    }

 */

}