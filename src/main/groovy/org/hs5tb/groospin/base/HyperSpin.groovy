package org.hs5tb.groospin.base

import org.hs5tb.groospin.common.IOTools
import org.hs5tb.groospin.common.Ini

/**
 * Created by Alberto on 12-Jun-16.
 */
class HyperSpin {
    File hsRoot
    File rlRoot

    HyperSpin(String hsRoot, String rlRoot) {
        this(new File(hsRoot), new File(rlRoot))
    }

    HyperSpin(File hsRoot, File rlRoot) {
        this.hsRoot = hsRoot.canonicalFile
        this.rlRoot = rlRoot.canonicalFile
        println("HyperSpin root: "+hsRoot)
        println("RocketLauncher root: "+rlRoot)
    }

/*

    String delimiter = ";"

    void listAllSystems() {
        listSystems(getSystems())
    }

    void listSystems(List systems) {
        log(["systemName","bytes","human size","db xml games","rom files","media:wheels","media:videos","media:themes","media:artwork1","media:artwork2","media:artwork3","media:artwork4"].join(delimiter))

        systems.each { String systemName ->
            haveSystem(systemName)
            CheckResult checkResult = checkAllGames(systemName)
            log(checkResult.getLongInfo(delimiter))
            endHave()
        }
    }

    void listSystem(String systemName) {
        listSystem(systemName, getGamesFromSystem(systemName))
    }

    void listSystem(String systemName, String game) {
        listSystem(systemName, [game])
    }

    void listSystem(String systemName, List games) {
        log(["systemName","bytes","human size","db xml games","rom files","media:wheels","media:videos","media:themes","media:artwork1","media:artwork2","media:artwork3","media:artwork4"].join(delimiter))
        haveSystem(systemName)
        CheckResult checkResult = check(systemName, games)
        log checkResult.getLongInfo(delimiter)
        endHave()
    }
*/
    Ini _cachedGlobalEmulatorIni

    Ini getGlobalEmulatorsIni() {
        if (_cachedGlobalEmulatorIni) return _cachedGlobalEmulatorIni
        File globalEmulatorConfig = new File(rlRoot, "Settings/Global Emulators.ini")
        _cachedGlobalEmulatorIni = new Ini().parse(globalEmulatorConfig)
        return _cachedGlobalEmulatorIni
    }

    RLSystem getSystem(String systemName) {
        File systemEmulatorConfig = new File(rlRoot, "Settings/${systemName}/Emulators.ini")
        if (!systemEmulatorConfig.file) {
            return null
        }
        Ini systemIni = new Ini().parse(systemEmulatorConfig)
        systemIni.parent = globalEmulatorsIni
        String rom_Path = systemIni.get("roms", "rom_path")
        String default_Emulator = systemIni.get("roms", "default_emulator")

        List romPathList = rom_Path?.split("\\|")?.collect { String romPathString -> IOTools.tryRelativeFrom(rlRoot, romPathString) }?:[]

        RLSystem system = new RLSystem(hyperSpin: this, name: systemName, iniRomPath : rom_Path,
            iniDefaultEmulator : default_Emulator, defaultEmulator : createEmulator(default_Emulator, systemIni.getSection(default_Emulator)), romPathsList : romPathList)
        system.loadMapping()
//        debug "$systemName romPathFiles " + rs.emuConfig.rom_Path
        return system
    }

    private RLEmulator createEmulator(String name, Map emulatorConfig) {
        String iniEmuPath = emulatorConfig['emu_path']
        String iniRomExtension = emulatorConfig['rom_extension']
        String module = emulatorConfig['module']
        File emuPath = iniEmuPath ? IOTools.tryRelativeFrom(rlRoot, iniEmuPath) : null
        List romExtensions = iniRomExtension?.split("\\|")?.collect { String ext -> ext.trim().toLowerCase() }?:[]
        RLEmulator emulator = new RLEmulator(name: name, iniEmuPath: iniEmuPath,
                iniRomExtension: iniRomExtension, emuPath: emuPath, romExtensions: romExtensions, module: module)
        return emulator
    }

    List listRomNames(String systemName) {
        File db = new File(hsRoot, "Databases/${systemName}/${systemName}.xml")
        if (!db.exists()) {
            error "${systemName} menu not found in ${db.absolutePath}"
            return []
        }
        def xml = new XmlParser().parseText(db.text)
        return xml.game.collect {
            if (it.@exe == "true") return null
            return it.@name
        }.findAll()
    }

    List listSystemNames() {
        listRomNames("Main menu")
    }

                  /*
    CheckResult checkGame(String systemName, String gameName) {
        CheckResult checkResult = check(systemName, [gameName])
        checkResult.game = gameName
        return checkResult
    }


    CheckResult checkAllGames(String systemName) {
        check(systemName, getGamesFromSystem(systemName))
    }

    */

}









