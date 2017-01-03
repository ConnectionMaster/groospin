package operation

import org.hs5tb.groospin.base.HyperSpin
import org.hs5tb.groospin.base.RLSystem
import org.hs5tb.groospin.common.IOTools

/**
 * Created by Alberto on 18-Oct-16.
 */
class RomMediaOperations extends Operations {

    RomMediaOperations(String hs) {
        super(hs)
    }

    RomMediaOperations(HyperSpin hs) {
        super(hs)
    }

    void copyMedia(String rom, String systemFromName, String systemToName, boolean overwrite = false) {
        RLSystem systemFrom = hyperSpin.getSystem(systemFromName)
        RLSystem systemTo = hyperSpin.getSystem(systemToName)
        copyMedia(rom, systemFrom, systemTo, overwrite)
    }

    void renameMedia(String rom, RLSystem systemFrom, String newRomName, boolean overwrite = false, boolean keepOriginal = false) {
        ["Video"].each { String path ->
            renameMedia(rom, systemFrom, newRomName, path, HyperSpin.VIDEO_EXTENSIONS, overwrite, keepOriginal)
        }

        ["Images/Wheel", "Images/Gamestart",
         "Images/Artwork1", "Images/Artwork2", "Images/Artwork3", "Images/Artwork4"].each { String path ->
            renameMedia(rom, systemFrom, newRomName, path, HyperSpin.IMAGE_EXTENSIONS, overwrite, keepOriginal)
        }

        ["Themes"].each { String path ->
            renameMedia(rom, systemFrom, newRomName, path, HyperSpin.THEME_EXTENSIONS, overwrite, keepOriginal)
        }

        ["Sound/Background Music"].each { String path ->
            renameMedia(rom, systemFrom, newRomName, "Sound/Background Music", HyperSpin.MUSIC_EXTENSIONS, overwrite, keepOriginal)
        }
    }

    void copyMedia(String rom, RLSystem systemFrom, RLSystem systemTo, boolean overwrite = false) {

        ["Video"].each { String path ->
            copyMedia(rom, systemFrom, systemTo, path, HyperSpin.VIDEO_EXTENSIONS, overwrite)
        }

        ["Images/Wheel", "Images/Gamestart",
         "Images/Artwork1", "Images/Artwork2", "Images/Artwork3", "Images/Artwork4"].each { String path ->
            copyMedia(rom, systemFrom, systemTo, path, HyperSpin.IMAGE_EXTENSIONS, overwrite)
        }

        ["Themes"].each { String path ->
            copyMedia(rom, systemFrom, systemTo, path, HyperSpin.THEME_EXTENSIONS, overwrite)
        }

        ["Sound/Background Music"].each { String path ->
            copyMedia(rom, systemFrom, systemTo, "Sound/Background Music", HyperSpin.MUSIC_EXTENSIONS, overwrite)
        }

    }

    void renameMedia(String rom, RLSystem systemFrom, String newRomName, String path, List extensions, boolean overwrite = false, boolean keepOriginal = false) {
        if (!overwrite) {
            File dst = IOTools.findFileWithExtensions(systemFrom.newMediaPath("${path}/${newRomName}"), extensions)
            if (dst) {
                return
            }
        }

        File originMedia = IOTools.findFileWithExtensions(systemFrom.newMediaPath("${path}/${rom}"), extensions)
        if (originMedia) {
            String originMediaExt = IOTools.getExtension(originMedia)
            File missingMedia = systemFrom.newMediaPath("${path}/${newRomName}.${originMediaExt}")
            log("(${simulation?"simulation":"real"}) ${originMedia} -> ${missingMedia}")
            if (!simulation) {
                if (keepOriginal) {
                    IOTools.copy(originMedia, missingMedia)
                } else {
                    IOTools.move(originMedia, missingMedia)
                }
            }
        }
    }

    void copyMedia(String rom, RLSystem systemFrom, RLSystem systemTo, String path, List extensions, boolean overwrite = false) {
        if (!overwrite) {
            File dst = IOTools.findFileWithExtensions(systemTo.newMediaPath("${path}/${rom}"), extensions)
            if (dst) {
                return
            }
        }

        File originMedia = IOTools.findFileWithExtensions(systemFrom.newMediaPath("${path}/${rom}"), extensions)
        if (originMedia) {
            File missingMedia = systemTo.newMediaPath("${path}/${originMedia.name}")
            log("(${simulation?"simulation":"real"}) ${originMedia} -> ${missingMedia}")
            if (!simulation) {
                IOTools.copy(originMedia, missingMedia)
            }

        }

    }
}
