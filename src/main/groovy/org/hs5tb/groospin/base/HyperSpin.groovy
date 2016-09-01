package org.hs5tb.groospin.base

import org.hs5tb.groospin.common.IOTools
import org.hs5tb.groospin.common.Ini
import org.xml.sax.SAXException

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
        println("HyperSpin root: " + hsRoot)
        println("RocketLauncher root: " + rlRoot)
    }

    Ini _cachedGlobalEmulatorIni

    Ini getGlobalEmulatorsIni() {
        if (_cachedGlobalEmulatorIni) return _cachedGlobalEmulatorIni
        File globalEmulatorConfig = findRocketLauncherFile("Settings/Global Emulators.ini")
        _cachedGlobalEmulatorIni = new Ini().parse(globalEmulatorConfig)
        return _cachedGlobalEmulatorIni
    }

    File findRocketLauncherFile(String name) {
        return IOTools.tryRelativeFrom(rlRoot, name)
    }

    File findHyperSpinFile(String name) {
        return IOTools.tryRelativeFrom(hsRoot, name)
    }

    Collection systemNames
    Collection allSystemNames
    Collection executableSystemNames

    RLSystem getSystem(String systemName) {
        listSystemNames()
        boolean isExecutable = systemName.toLowerCase() in executableSystemNames.collect { it.toLowerCase() }
        File systemEmulatorConfig = findRocketLauncherFile("Settings/${systemName}/Emulators.ini")
        if (!systemEmulatorConfig.file) {
            throw new FileNotFoundException("RocketLauncher settings for ${systemName} not found: ${systemEmulatorConfig}")
        }
        Ini systemIni = new Ini().parse(systemEmulatorConfig)
        systemIni.parent = globalEmulatorsIni
        String rom_Path = systemIni.get("roms", "rom_path")
        String default_emulator = systemIni.get("roms", "default_emulator")

        List romPathList = rom_Path?.split("\\|")?.collect { String romPathString -> IOTools.tryRelativeFrom(rlRoot, romPathString) } ?: []

        File alternativeEmulatorConfig = findRocketLauncherFile("Settings/${systemName}/Games.ini")
        Map alternativeEmulators = [:]
        if (alternativeEmulatorConfig.file) {
            Ini alternativeEmulatorsIni = new Ini().parse(alternativeEmulatorConfig)
            alternativeEmulatorsIni.getSections().each { String gameName, Map alternativeEmulatorForGameConfig ->
                String emulatorName = alternativeEmulatorForGameConfig.get(Ini.canonical("Emulator"))
                if (emulatorName) {
                    alternativeEmulators[gameName] = findOrCreateEmulator(emulatorName, systemIni.getSection(emulatorName))
                }
            }
        }

        RLSystem system = new RLSystem(alternativeEmulators: alternativeEmulators, hyperSpin: this, name: systemName, iniRomPath: rom_Path, executable: isExecutable,
                iniDefaultEmulator: default_emulator, defaultEmulator: findOrCreateEmulator(default_emulator, systemIni.getSection(default_emulator)), romPathsList: romPathList)
        system.loadMapping()
        return system
    }

    Map emulators = [:]
    private RLEmulator findOrCreateEmulator(String name, Map emulatorConfig) {
        if (emulators[name]) {
            return emulators[name]
        }
        String iniEmuPath = emulatorConfig['emu_path']
        String iniRomExtension = emulatorConfig['rom_extension']
        String module = emulatorConfig['module']
        File emuPath = iniEmuPath ? IOTools.tryRelativeFrom(rlRoot, iniEmuPath) : null
        List romExtensions = iniRomExtension?.split("\\|")?.collect { String ext -> ext.trim().toLowerCase() } ?: []
        RLEmulator emulator = new RLEmulator(name: name, iniEmuPath: iniEmuPath,
                iniRomExtension: iniRomExtension, emuPath: emuPath, romExtensions: romExtensions, module: module)
        emulators[name] = emulator
        return emulator
    }

    HyperSpinDatabase loadHyperSpinDatabase(String systemName, Closure filter = null) {
        File db = findSystemDatabaseFile(systemName)
        if (!db.exists()) {
            return new HyperSpinDatabase().load(db, filter)
        }
        return null
    }

    List<Rom> listRoms(String systemName, Collection<String> names = null) {
        Set canonicalNames = names ? names.collect { it.trim().toLowerCase() } as Set : null
        databaseCollect(systemName) { Node node ->
            return (canonicalNames == null || node.@name?.trim()?.toLowerCase() in canonicalNames) ? new Rom().loadFromHyperspinDatabase(node) : null
        }
    }

    List<String> listSystemNames(boolean includeExecutables = false) {
        if (systemNames == null) {
            systemNames = _listSystemNames(false)
            allSystemNames = _listSystemNames(true)
            executableSystemNames = allSystemNames - systemNames
        }
        return includeExecutables ? allSystemNames : systemNames
    }

    private List<String> _listSystemNames(boolean includeExecutables) {
        databaseCollect("Main menu") { Node node ->
            if (!includeExecutables && node.@exe == "true") return null
            return node.@name
        }
    }

    List<String> listRomNames(String systemName) {
        databaseCollect(systemName) { Node node ->
            return node.@name
        }
    }

    Rom getRom(String systemName, String rom) {
        List<Rom> roms = listRoms(systemName, [rom])
        roms ? roms.first() : null
    }

    List databaseCollect(String systemName, Closure filter) {
        File db = findSystemDatabaseFile(systemName)
        if (!db.exists()) {
            return [] // throw new FileNotFoundException("${systemName} menu not found in ${db.absolutePath}")
        }
        Node menu = new XmlParser().parseText(db.text)
        return menu.game.collect(filter).findAll()
    }

    File findSystemDatabaseFile(String systemName) {
        return findHyperSpinFile("Databases/${systemName}/${systemName}.xml")
    }

    Collection<RLSystem> listSystems() {
        return listSystemNames().collect { String system ->
            getSystem(system)
        }
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

    File getRocketLauncherExe() {
        findRocketLauncherFile("RocketLauncher.exe")
    }


}









