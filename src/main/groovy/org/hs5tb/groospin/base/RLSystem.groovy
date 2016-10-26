package org.hs5tb.groospin.base

import org.hs5tb.groospin.common.IOTools
import org.hs5tb.groospin.common.Ini
import org.hs5tb.groospin.common.IniFile

/**
 * Created by Alberto on 12-Jun-16.
 */
class RLSystem {
    HyperSpin hyperSpin
    String name
    RLEmulator defaultEmulator
    boolean executable = false

    String iniRomPath
    String iniDefaultEmulator
    List<File> romPathsList

    Ini romMapping
    Map<String, RLEmulator> alternativeEmulators

    File findValidRom(String romName) {
        RLEmulator emulator = findRomEmulator(romName)

        if (emulator.module == "ScummVM.ahk") {
            String path = romMapping.get(romName, "path")
            if (path) {
                File scummRomFolder = hyperSpin.findRocketLauncherFile(path)
                if (scummRomFolder.exists() && scummRomFolder.directory) {
                    return scummRomFolder
                }
            }
            return null
        }

        for (String ext in emulator.romExtensions) {
            File rom = IOTools.findFileInFolders(romPathsList, romName+ "." + ext)
            rom = rom ?: IOTools.findFileInFolders(romPathsList, romName + "/" + romName + "." + ext)
            if (rom) return rom
        }
        return null
    }

    boolean romsIsExecutable(String romName) {
        RLEmulator emulator = findRomEmulator(romName)
        return emulator.module in ["MUGEN.ahk", "OpenBOR.ahk", "Casual Games.ahk", "PCLauncher.ahk"]
    }

    File findSystemDatabaseFile() {
        hyperSpin.findSystemDatabaseFile(name)
    }

    File findExecutable(String romName, File romFile) {
        RLEmulator emulator = findRomEmulator(romName)
        File romPath = romFile.directory ? romFile : romFile.parentFile
        if (emulator.module == "PCLauncher.ahk") {
            String application = romMapping.get(romName, "Application")
            return application ? hyperSpin.findRocketLauncherFile(application) : null
        } else if (emulator.module == "MUGEN.ahk") {
            String iniGamePath = romMapping.get(romName, "gamePath")
            List<String> candidates = iniGamePath ? ["/" + iniGamePath] : []
            candidates.addAll(["/" + romName + "/MUGEN.exe"])
            return IOTools.findFilesInFolder(romPath, candidates)
        } else if (emulator.module == "OpenBOR.ahk") {
            String iniGamePath = romMapping.get(romName, "gamePath")
            List<String> candidates = iniGamePath ? ["/" + iniGamePath] : []
            candidates.addAll(["/" + romName + "/OpenBOR.exe", "/OpenBOR.exe"])
            return IOTools.findFilesInFolder(romPath, candidates)
        } else if (emulator.module == "Casual Games.ahk") {
            String iniGamePath = romMapping.get(romName, "gamePath")
            if (iniGamePath && new File(iniGamePath).exists()) return new File(iniGamePath).canonicalFile
            List<String> candidates = iniGamePath ? ["/" + iniGamePath] : []
            candidates.addAll(["/" + romName +".exe", "/" + romName + "/" + romName +".exe"])
            return IOTools.findFilesInFolder(romPath, candidates)
        }
        return null
    }


    void loadMapping() {
        if (!defaultEmulator) return
        if (defaultEmulator.module == "PCLauncher.ahk") {
            romMapping = new IniFile().parse(
                    hyperSpin.findRocketLauncherFile("Modules/PCLauncher/${name}.ini"))
            romMapping.parent = new IniFile().parse(
                    hyperSpin.findRocketLauncherFile("Modules/PCLauncher/PCLauncher.ini"))
        } else if (defaultEmulator.module == "ScummVM.ahk") {
            IniFile moduleIni = new IniFile().parse(hyperSpin.findRocketLauncherFile("Modules/ScummVM/ScummVM.ini").text, "Settings", ["CustomConfig"])
            String customConfig = moduleIni.get("Settings", "CustomConfig")
            romMapping = new IniFile()
            if (customConfig) {
                romMapping = romMapping.parse(hyperSpin.findRocketLauncherFile(customConfig))
            }
        } else if (defaultEmulator.module == "Casual Games.ahk") {
            romMapping = new IniFile().parse(hyperSpin.findRocketLauncherFile("Modules/Casual Games/Casual Games.ini"))
        } else if (defaultEmulator.module == "OpenBOR.ahk") {
            romMapping = new IniFile().parse(hyperSpin.findRocketLauncherFile("Modules/OpenBOR/OpenBOR.ini"))
        } else if (defaultEmulator.module == "MUGEN.ahk") {
            romMapping = new IniFile().parse(hyperSpin.findRocketLauncherFile("Modules/MUGEN/MUGEN.ini"))
        }
    }

    Rom findRom(String name) {
        listRomNames([name])
    }

    List<Rom> listRoms(Collection<String> names = null) {
        return hyperSpin.listRoms(name, names)
    }

    List<String> listRomNames() {
        return hyperSpin.listRomNames(name)
    }

    List<String> listGenres() {
        return hyperSpin.listGenres(name)
    }

    RLEmulator findRomEmulator(String romName) {
        RLEmulator emulator = alternativeEmulators?.get(romName) ?: defaultEmulator
        return emulator
    }

    HyperSpinDatabase loadHyperSpinDatabase(Closure romFilter = null) {
        return hyperSpin.loadHyperSpinDatabase(name, romFilter)
    }

    IniFile loadHyperSpinSettings() {
        return hyperSpin.loadHyperSpinSettings(name)
    }

    void changeHyperSpinSettings(String section, String key, String newValue) {
        hyperSpin.changeHyperSpinSettings(name, section, key, newValue)
    }

    File getMediaFolder() {
        return hyperSpin.findHyperSpinMediaFolderFor(name)
    }

    File getMediaPath(String path) {
        return new File(mediaFolder, path)
    }

    File[] listMediaVideo(boolean includeFolders = false) {
        return listMediaPath("Video", includeFolders)
    }

    File[] listMediaWheel(boolean includeFolders = false) {
        return listMediaPath("Images/Wheel", includeFolders)
    }

    File[] listMediaArtwork1(boolean includeFolders = false) {
        return listMediaPath("Images/Artwork1", includeFolders)
    }

    File[] listMediaArtwork2(boolean includeFolders = false) {
        return listMediaPath("Images/Artwork2", includeFolders)
    }

    File[] listMediaArtwork3(boolean includeFolders = false) {
        return listMediaPath("Images/Artwork3", includeFolders)
    }

    File[] listMediaArtwork4(boolean includeFolders = false) {
        return listMediaPath("Images/Artwork4", includeFolders)
    }

    File[] listMediaImagesGamestart(boolean includeFolders = false) {
        return listMediaPath("Images/Gamestart", includeFolders)
    }

    File[] listMediaThemes(boolean includeFolders = false) {
        return listMediaPath("Themes", includeFolders)
    }

    File[] listMediaBackgroundMusic(boolean includeFolders = false) {
        return listMediaPath("Sound/Background Music", includeFolders)
    }

    File[] listMediaPath(String path, boolean includeFolders = false) {
        return getMediaPath(path).listFiles( new FileFilter() {
            @Override
            boolean accept(File pathname) {
                return includeFolders || pathname.file
            }
        })
    }

    List allowedMediaRomThemes() {
        return (listRomNames()+["default"]).collect { it+".zip"}
    }

    List allowedMediaRomMusic() {
        return listRomNames().collect { it+".mp3"}
    }

    List allowedMediaRomImages() {
        return explodeFilenameExtensions(listRomNames(), ["jpg", "png"])
    }

    List allowedMediaRomVideos() {
        return explodeFilenameExtensions(listRomNames(), ["flv", "mp4"])
    }

    static private List explodeFilenameExtensions(List names, List extensions) {
        return names.collect { String name -> extensions.collect { String extension -> name+"."+extension } }.flatten()
    }
}

