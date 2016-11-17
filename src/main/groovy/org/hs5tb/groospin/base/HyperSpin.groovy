package org.hs5tb.groospin.base

import org.hs5tb.groospin.common.IOTools
import org.hs5tb.groospin.common.Ini
import org.hs5tb.groospin.common.IniFile
import org.hs5tb.groospin.common.IniFile

/**
 * Created by Alberto on 12-Jun-16.
 */
class HyperSpin {
    static List IMAGE_EXTENSIONS = ["png", "jpg"]
    static List VIDEO_EXTENSIONS = ["mp4", "flv"]
    static List THEME_EXTENSIONS = ["zip"]
    static List MUSIC_EXTENSIONS = ["mp3"]
    File hsRoot
    File rlRoot

    Ini _cachedGlobalEmulatorIni
    Ini rocketLauncherIni

    HyperSpin(String rlRoot) {
        this(new File(rlRoot))
    }

    HyperSpin(File rlRoot) {
        this.rlRoot = rlRoot.canonicalFile
        rocketLauncherIni = new IniFile().parse(findRocketLauncherFile("Settings/RocketLauncher.ini"))
        if (rocketLauncherIni.get("Settings", "Default_Plugin") != "HyperSpin") {
            throw new IllegalArgumentException("RocketLauncher is not configured with HyperSpin")
        }
        String hsRoot = rocketLauncherIni.get("Settings", "Default_Front_End_Path")
        this.hsRoot = findRocketLauncherFile(hsRoot).canonicalFile.parentFile
    }

    Ini getGlobalEmulatorsIni() {
        if (_cachedGlobalEmulatorIni) return _cachedGlobalEmulatorIni
        File globalEmulatorConfig = findRocketLauncherFile("Settings/Global Emulators.ini")
        _cachedGlobalEmulatorIni = new IniFile().parse(globalEmulatorConfig)
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
        if (isExecutable) {
            File systemSettingsConfig = findHyperSpinFile("Settings/${systemName}.ini")
            if (!systemSettingsConfig.file) {
                throw new FileNotFoundException("HyperSpin settings for ${systemName} not found: ${systemSettingsConfig}")
            }
            Ini settings = new IniFile().parse(systemSettingsConfig)
            String path = settings.get("exe info", "path")

            RLSystem system = new RLSystem(hyperSpin: this, name: systemName, iniRomPath: path, executable: true, romPathsList: [findRocketLauncherFile(path)])
            return system
        }

        File systemEmulatorConfig = findRocketLauncherFile("Settings/${systemName}/Emulators.ini")
        if (!systemEmulatorConfig.file) {
            throw new FileNotFoundException("RocketLauncher settings for ${systemName} not found: ${systemEmulatorConfig}")
        }
        Ini systemIni = new IniFile().parse(systemEmulatorConfig)
        systemIni.parent = globalEmulatorsIni
        String rom_Path = systemIni.get("ROMS", "Rom_Path")
        String default_emulator = systemIni.get("ROMS", "Default_Emulator")

        List romPathList = rom_Path?.split("\\|")?.collect { String romPathString -> findRocketLauncherFile(romPathString) } ?: []

        File alternativeEmulatorConfig = findRocketLauncherFile("Settings/${systemName}/Games.ini")
        Map alternativeEmulators = [:]
        if (alternativeEmulatorConfig.file) {
            Ini alternativeEmulatorsIni = new IniFile().parse(alternativeEmulatorConfig)
            alternativeEmulatorsIni.getSections().each { String gameName, Map alternativeEmulatorForGameConfig ->
                String emulatorName = alternativeEmulatorForGameConfig.get("Emulator")
                if (emulatorName) {
                    alternativeEmulators[gameName] = findOrCreateEmulator(emulatorName, systemIni.getSection(emulatorName))
                }
            }
        }

        RLSystem system = new RLSystem(alternativeEmulators: alternativeEmulators, hyperSpin: this, name: systemName, iniRomPath: rom_Path, executable: false,
                iniDefaultEmulator: default_emulator, defaultEmulator: findOrCreateEmulator(default_emulator, systemIni.getSection(default_emulator)), romPathsList: romPathList)
        system.loadMapping()
        return system
    }

    Map emulators = [:]
    private RLEmulator findOrCreateEmulator(String name, Map emulatorConfig) {
        if (emulators[name]) {
            return emulators[name]
        }
        String iniEmuPath = emulatorConfig['Emu_Path']
        String iniRomExtension = emulatorConfig['Rom_Extension']
        String module = emulatorConfig['Module']
        File emuPath = iniEmuPath ? findRocketLauncherFile(iniEmuPath) : null
        List romExtensions = iniRomExtension?.split("\\|")?.collect { String ext -> ext.trim().toLowerCase() } ?: []
        RLEmulator emulator = new RLEmulator(name: name, iniEmuPath: iniEmuPath,
                iniRomExtension: iniRomExtension, emuPath: emuPath, romExtensions: romExtensions, module: module)
        emulators[name] = emulator
        return emulator
    }

    HyperSpinDatabase loadHyperSpinDatabase(String systemName, Closure filter = null) {
        File db = findSystemDatabaseFile(systemName)
        if (db.exists()) {
            return new HyperSpinDatabase().load(db, filter)
        }
        return null
    }

    List<Rom> listRoms(String systemName, Collection<String> names = null) {
        Set canonicalNames = names ? names.collect { it.trim().toLowerCase() } as Set : null
        databaseCollect(systemName) { Node node ->
            return (canonicalNames == null || node.@name?.trim()?.toLowerCase() in canonicalNames) ? new Rom().loadFromHyperSpinDatabase(node) : null
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
        Node menu = new XmlParser().parse(db.newReader())
        return menu.game.collect(filter).findAll()
    }

    File findSystemDatabaseFile(String systemName) {
        return findHyperSpinFile("Databases/${systemName}/${systemName}.xml")
    }

    Collection<RLSystem> listSystems(boolean includeExecutables = false) {
        return listSystemNames(includeExecutables).collect { String system ->
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

    IniFile loadRocketLauncherIni(String path) {
        File file = findRocketLauncherFile(path)
        if (file.exists()) {
            return new IniFile().parse(file)
        }
        return null
    }

    IniFile loadHyperSpinSettings(String filename) {
        File file = findHyperSpinFile("Settings/${filename}.ini")
        if (file.exists()) {
            return new IniFile().parse(file)
        }
        return null
    }

    void changeHyperSpinAllSystemSettings(String section, String key, String newValue) {
        changeHyperSpinSettings(listSystemNames(true), section, key, newValue)
    }

    void changeHyperSpinSettings(String filename, String section, String key, String newValue) {
        changeHyperSpinSettings([filename], section, key, newValue)
    }

    void changeHyperSpinSettings(List filenames, String section, String key, String newValue) {
        withHyperSpinSettings(filenames) { IniFile ini ->
            ini.put(section, key, newValue)
            if (ini.dirty) {
                ini.store()
            }
        }
    }

    void withHyperSpinAllSystemSettings(Closure action) {
        withHyperSpinSettings(listSystemNames(true), action)
    }

    void withHyperSpinSettings(String filename, Closure action) {
        withHyperSpinSettings([filename], action)
    }

    void withHyperSpinSettings(List filenames, Closure action) {
        filenames.each { String filename ->
            IniFile ini = loadHyperSpinSettings(filename)
            if (ini) {
                if (action.maximumNumberOfParameters == 1) {
                    action.call(ini)
                } else {
                    action.call(filename, ini)
                }

            }
        }
    }

    void withRocketLauncherInis(List filenames, Closure action) {
        filenames.each { String filename ->
            IniFile ini = loadRocketLauncherIni(filename)
            if (ini) {
                if (action.maximumNumberOfParameters == 1) {
                    action.call(ini)
                } else {
                    action.call(filename, ini)
                }

            }
        }
    }

    File findHyperSpinMediaFolderFor(String name) {
        return findHyperSpinFile("Media/${name}")
    }

    List<String> listGenres(String systemName) {
        File genres = findHyperSpinFile("Databases/${systemName}/genre.xml")
        HyperSpinDatabase.listRomNames(genres)
    }
}









