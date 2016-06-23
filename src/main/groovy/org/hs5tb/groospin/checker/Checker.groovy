/*
 * hs5tb
 *
 * @author Alberto Vilches (alberto.vilches@hs5tb)
 * @date 13/6/16
 * Copyright. All Rights Reserved.
 */
package org.hs5tb.groospin.checker

import groovy.transform.CompileStatic
import org.hs5tb.groospin.base.HyperSpin
import org.hs5tb.groospin.base.RLSystem
import org.hs5tb.groospin.base.Rom
import org.hs5tb.groospin.checker.result.CheckResult
import org.hs5tb.groospin.checker.result.CheckRomResult
import org.hs5tb.groospin.checker.result.CheckTotalResult
import org.hs5tb.groospin.common.IOTools;

@CompileStatic
class Checker {

    HyperSpin hyperSpin
    Collection<CheckHandler> handlers = []
    Boolean calculateRomSize = null
    Boolean calculateMediaSize = null
    RomChecker romChecker = new RomChecker()

    Checker(HyperSpin hyperSpin) {
        this.hyperSpin = hyperSpin
    }

    Checker(HyperSpin hyperSpin, CheckHandler handler) {
        this.hyperSpin = hyperSpin
        addHandler(handler)
    }

    Checker addHandler(CheckHandler handler) {
        handlers << handler
        resetCachedFlags()
        return this
    }

    void resetCachedFlags() {
        calculateRomSize = null
        calculateMediaSize = null
    }

    void checkSystems(List<String> systems = null) {
        if (!systems) systems = hyperSpin.listSystemNames()
        wrap {
            CheckTotalResult checkResultTotal = new CheckTotalResult()
            systems.each { String systemName ->
                CheckTotalResult checkResultSystem = checkSystemRoms(systemName)
                checkResultTotal.add(checkResultSystem)
            }
            return checkResultTotal
        }
    }

    void checkSystem(String systemName, String rom) {
        checkSystem(systemName, [rom])
    }

    void checkSystem(String systemName, List<String> romNames = null) {
        wrap {
            return checkSystemRoms(systemName, romNames)
        }
    }

    void wrap(Closure<CheckTotalResult> closure) {
        resetCachedFlags()
        handlers*.startCheck()
        CheckTotalResult total = closure.call()
        handlers*.endCheck(total)
    }

    long calculateMediaPathSize(RLSystem system) {
        if (calculateMediaSize == null) {
            calculateMediaSize = handlers ? handlers.any { it.needsMediaFolderSize() } : false
        }
        if (calculateMediaSize) {
            return IOTools.folderSize(hyperSpin.findHyperSpinFile("Media/${system.name}"))
        }
        return 0
    }

    long calculateRomPathSize(RLSystem system) {
        if (calculateRomSize == null) {
            calculateRomSize = handlers ? handlers.any { it.needsRomFolderSize() } : false
        }
        long totalSize = 0
        if (calculateRomSize) {
            system.romPathsList?.each { File romFolder -> totalSize += IOTools.folderSize(romFolder) }
        }
        return totalSize

    }

    CheckTotalResult checkSystemRoms(String systemName, Collection<String> romNames = null) {
        CheckTotalResult checkTotalResult = new CheckTotalResult(systemName: systemName)
        try {
            RLSystem system = hyperSpin.getSystem(systemName)
            handlers*.startSystem(system)
            List<Rom> roms = system.listRoms(romNames)
            checkTotalResult.systemName = system.name
            checkTotalResult.system = system
            checkTotalResult.totalRoms = roms.size()
            checkTotalResult.totalRomSize = calculateRomPathSize(system)
            checkTotalResult.totalMediaSize = calculateMediaPathSize(system)

            roms.sort{ it.name }.each { Rom rom ->
                CheckResult checkResultRom = romChecker.check(system, rom.name)
                checkResultRom.rom = rom
                checkTotalResult.add(checkResultRom)
                handlers*.romChecked(checkResultRom)
            }
            handlers*.endSystem(checkTotalResult)
        } catch (e) {
            handlers*.endSystemWithError(systemName, e)
        }
        return checkTotalResult
    }

    CheckRomResult checkRom(RLSystem system, String rom) {
        romChecker.check(system, rom)
    }
}