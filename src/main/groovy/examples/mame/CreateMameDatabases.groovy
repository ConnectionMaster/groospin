package examples.mame

import groovy.transform.Field
import mame.DatXmlToHyperSpinXml
import operation.Comparer
import org.hs5tb.groospin.base.HyperSpinDatabase
import org.hs5tb.groospin.base.MameMachine

def mame171dat = "d:/Games/Emulators/MAME/MameUIFX_0.171_64bits_nonag-dinput/dat.xml"
def catver = "d:/Games/Soft/GrooSpin/resources/pS_CatVer/176/catver.ini"
def extraInfo = "d:/Games/Soft/GrooSpin/resources/Official HyperSpin MAME/code/extra_info.txt"
def header = [listversion    : "0.171",
              lastlistupdate : new Date().format("dd/MM/yyyy"),
              exporterversion: "GrooSpin by HS5Tb"]

def databaseOriginal = "d:/Games/HyperSpin-fe/Databases alternativas/MAME/oficiales"
def roms = DatXmlToHyperSpinXml.load(mame171dat, catver, extraInfo)
/*
debugRoms.removeAll { it.mahjong || it.hanafuda }
debugRoms.removeAll { it.catVerCat?.contains("Casino") || it.genre?.contains("Casino") }
debugRoms.removeAll { it.catVerCat?.contains("Tabletop") || it.genre?.contains("Tabletop") }
debugRoms.removeAll { it.catVerCat?.contains("Tabletop") || it.genre?.contains("Mahjong") }
*/

generateAll(roms, header, "d:/Games/HyperSpin-fe/Databases alternativas/MAME/0171/todo", databaseOriginal) { MameMachine rom ->
    true
}

generateAll(roms, header, "d:/Games/HyperSpin-fe/Databases alternativas/MAME/0171/sin clones", databaseOriginal) { MameMachine rom ->
    !rom.cloneof
}

generateAll(roms, header, "d:/Games/HyperSpin-fe/Databases alternativas/MAME/0171/sin clones solo joystick", databaseOriginal) { MameMachine rom ->
    !rom.cloneof &&
            rom.hasJoystick() && !rom.hasBall() && !rom.hasKeyboard() &&
            !rom.hanafuda && !rom.gambling && !rom.mahjong
}

generateAll(roms, header, "d:/Games/HyperSpin-fe/Databases alternativas/MAME/0171/sin clones ni chds solo joystick", databaseOriginal) { MameMachine rom ->
    !rom.cloneof &&
            rom.hasJoystick() && !rom.hasBall() && !rom.hasKeyboard() &&
            !rom.hanafuda && !rom.gambling && !rom.mahjong &&
            !rom.disks
}


void generateAll(List roms, Map header, String dst, String databaseOriginal, Closure filter) {
    // MAME solo working
    DatXmlToHyperSpinXml.store(roms,
            "${dst}/MAME/MAME.xml",
            header + [listname: "MAME only working"]) { MameMachine rom ->
        return filter(rom) && rom.working
    }
    Comparer.printDifferences(
            "${dst}/MAME/MAME.xml",
            "d:/Games/Soft/GrooSpin/resources/r0man0 171/Mame/Working Games/Mame.xml")
    Comparer.printDifferences("MAME", dst, databaseOriginal)

    Set bestOfMameRomNames = (griffinBestOf() + redditBestOf()) as Set
    // MAME best of (solo working)
    DatXmlToHyperSpinXml.store(roms,
            "${dst}/Best of MAME/Best of MAME.xml",
            header + [listname: "Best of MAME only working"]) { MameMachine rom ->
        return filter(rom) && rom.working && rom.name in bestOfMameRomNames
    }
    Comparer.printDifferences(
            "${dst}/MAME/MAME.xml",
            "d:/Games/Soft/GrooSpin/resources/r0man0 171/Mame/Working Games/Mame.xml")
    Comparer.printDifferences("MAME", dst, databaseOriginal)

    // MAME 4 Players
    DatXmlToHyperSpinXml.store(roms,
            "${dst}/MAME 4 Players/MAME 4 Players.xml",
            header + [listname: "MAME 4 Players only working with clones (mechanical removed)"]) { MameMachine rom ->
        return filter(rom) && rom.working && rom.players >= 3
    }
    Comparer.printDifferences("MAME 4 Players", dst, databaseOriginal)

    // Otros sistemas
    DatXmlToHyperSpinXml.store(roms,
            "${dst}/SNK Classics/SNK Classics.xml",
            header + [listname: "SNK Classics (NeoGeo roms removed)"]) { MameMachine rom ->
        // SNK Classics needs an special condition to remove the neogeo roms
        return filter(rom) && rom.working && rom.manufacturer.contains("SNK") && rom.romof != "neogeo" && rom.romof != "hng64"
    }
    Comparer.printDifferences(
            "${dst}/SNK Classics/SNK Classics.xml",
            "d:/Games/Soft/GrooSpin/resources/r0man0 171/Manufacturers/SNK Classics/Working Games/SNK Classics.xml")
    Comparer.printDifferences("SNK Classics", dst, databaseOriginal)

    DatXmlToHyperSpinXml.store(roms,
            "${dst}/Nintendo Classics/Nintendo Classics.xml",
            header + [listname: "Nintendo Classics"]) { MameMachine rom ->
        return filter(rom) && rom.working && (rom.manufacturer.contains("Nintendo") || rom.sourcefile == "vsnes.cpp")
    }
    Comparer.printDifferences(
            "${dst}/Nintendo Classics/Nintendo Classics.xml",
            "d:/Games/Soft/GrooSpin/resources/r0man0 171/Manufacturers/Nintendo Classics/Working Games/Nintendo Classics.xml")
    Comparer.printDifferences("Nintendo Classics", dst, databaseOriginal)

    DatXmlToHyperSpinXml.store(roms,
            "${dst}/SNK Neo Geo AES/SNK Neo Geo AES.xml",
            header + [listname: "SNK Neo Geo AES"]) { MameMachine rom ->
        // NeoGeo don't remove the non-working games
        return filter(rom) && rom.romof == "neogeo"
    }
    Comparer.printDifferences("SNK Neo Geo AES", dst, databaseOriginal)

    DatXmlToHyperSpinXml.store(roms,
            "${dst}/HyperNeoGeo64/HyperNeoGeo64.xml",
            header + [listname: "HyperNeoGeo64"]) { MameMachine rom ->
        // HyperNeoGeo64 don't remove the non-working games
        return filter(rom) && rom.romof == "hng64"
    }
    Comparer.printDifferences("HyperNeoGeo64", dst, databaseOriginal)

    DatXmlToHyperSpinXml.store(roms,
            "${dst}/Capcom Play System/Capcom Play System.xml",
            header + [listname: "Capcom Play System"]) { MameMachine rom ->
        return filter(rom) && rom.working && rom.sourcefile == "cps1.cpp"
    }
    Comparer.printDifferences("Capcom Play System", dst, databaseOriginal)


    DatXmlToHyperSpinXml.store(roms,
            "${dst}/Capcom Play System II/Capcom Play System II.xml",
            header + [listname: "Capcom Play System II"]) { MameMachine rom ->
        return filter(rom) && rom.working && rom.sourcefile == "cps2.cpp"
    }
    Comparer.printDifferences("Capcom Play System II", dst, databaseOriginal)

    DatXmlToHyperSpinXml.store(roms,
            "${dst}/Capcom Play System III/Capcom Play System III.xml",
            header + [listname: "Capcom Play System III"]) { MameMachine rom ->
        return filter(rom) && rom.working && rom.sourcefile == "cps3.cpp"
    }
    Comparer.printDifferences("Capcom Play System III", dst, databaseOriginal)

    DatXmlToHyperSpinXml.store(roms,
            "${dst}/Sega ST-V/Sega ST-V.xml",
            header + [listname: "Sega ST-V"]) { MameMachine rom ->
        return filter(rom) && rom.sourcefile == "stv.cpp"
    }
    Comparer.printDifferences("Sega ST-V", dst, databaseOriginal)

    DatXmlToHyperSpinXml.store(roms,
            "${dst}/Namco System 22/Namco System 22.xml",
            header + [listname: "Namco System 22"]) { MameMachine rom ->
        return filter(rom) && rom.working && rom.sourcefile == "namcos22.cpp"
    }
    Comparer.printDifferences("Namco System 22", dst, databaseOriginal)


    ["Namco Classics",
     "Atari Classics", "Capcom Classics", "Cave", "Data East Classics",
     "Banpresto", "Kaneko", "Irem Classics", "Williams Classics", "Midway Classics",
     "Sega Classics", "Konami Classics", "Taito Classics"].sort().each {

        String find = it - " Classics"

        DatXmlToHyperSpinXml.store(roms,
                "${dst}/${it}/${it}.xml",
                header + [listname: "${it}"]) { MameMachine rom ->
            return filter(rom) && rom.working && rom.manufacturer.contains(find) &&
                    rom.sourcefile != "namcos22.cpp" && // quita los de Namco System 22 de Namco Classics
                    rom.sourcefile != "stv.cpp" && // Quita los de Sega ST-V de Sega Classics
                    rom.sourcefile != "cps1.cpp" && // Quita los de Capcom System 1
                    rom.sourcefile != "cps2.cpp" && // Quita los de Capcom System 2
                    rom.sourcefile != "cps3.cpp" && // Quita los de Capcom System 3
                    rom.romof != "neogeo" && // Quita los de Neo Geo
                    rom.romof != "hng64"  // Quita los de Hyper NeoGeo 64
        }

        Comparer.printDifferences(it, dst, databaseOriginal)
        Comparer.printDifferences(
                "${dst}/${it}/${it}.xml",
                "d:/Games/Soft/GrooSpin/resources/r0man0 171/Manufacturers/${it}/Working Games/${it}.xml")

    }


}

def griffinBestOf() {
    HyperSpinDatabase best = new HyperSpinDatabase().load(new File("d:\\Games\\Soft\\GrooSpin\\resources\\bestof\\griffin-MAME178.xml"))
    return best.roms*.name

}

def redditBestOf() {
    // From https://www.reddit.com/r/MAME/comments/2rawpr/i_compiled_several_best_ofrecommended_arcade/
    return ["1943",
            "1944",
            "2020bb",
            "9ballsht",
            "avsp",
            "alien3",
            "alpham2",
            "altbeast",
            "area51",
            "arkangc",
            "batrider",
            "asteroid",
            "astdelux",
            "baddudes",
            "bstars2",
            "batsugun",
            "bbakraidja",
            "batcir",
            "bgaregga",
            "berzerk",
            "blktiger",
            "bwidow",
            "blazstar",
            "bloodbro",
            "bombjack",
            "bjtwin",
            "boogwing",
            "bublboblr",
            "bubsymphu",
            "bucky",
            "btime",
            "cabal",
            "dino",
            "captaven",
            "captcomm",
            "carnevil",
            "cninja",
            "centipdb",
            "choplift",
            "contra",
            "crimfght",
            "cyberbal",
            "dfeveron",
            "dariusg",
            "dstlk",
            "defender",
            "digdug",
            "dimahoo",
            "ddonpach",
            "dkong",
            "dkongx",
            "dkongjrb",
            "donpachi",
            "ddragonu",
            "dragnblz",
            "dbreed",
            "drgnmst",
            "dsaber",
            "dungeonm",
            "ddtod",
            "eagle",
            "elevator",
            "elvactr",
            "esprade",
            "fatfury1",
            "ffight",
            "footchmp",
            "frogger",
            "funkyjet",
            "gaiapols",
            "galaga",
            "galaxrf",
            "gauntlet",
            "gauntdl",
            "gauntleg",
            "gng",
            "ghouls",
            "goldnaxe",
            "mp_gaxe2",
            "gtg",
            "gorf",
            "gberet",
            "grdians",
            "gnbarich",
            "gunbird2",
            "guwange",
            "gyruss",
            "inthunt",
            "indytemp",
            "jojobane",
            "joust",
            "junglek",
            "karianx",
            "kinst",
            "kinst2",
            "kotm",
            "knights",
            "kungfum",
            "lethalen",
            "llander",
            "magdrop3",
            "mhavoc",
            "mappy",
            "mmatrix",
            "mshvsf",
            "mvsc",
            "mslug",
            "mslug2",
            "mslug3",
            "mslug4",
            "ms5pcb",
            "mslugx",
            "metamrph",
            "mwalk",
            "missile",
            "mpatrol",
            "mk",
            "mk3",
            "mk2",
            "mtrap",
            "docastle",
            "mspacmab",
            "nam1975",
            "nbajam",
            "nbajamte1",
            "blitz",
            "nwarr",
            "nbbatman",
            "nspirit",
            "outrun",
            "pacman",
            "pang3",
            "pengo",
            "pgoal",
            "polepos",
            "pong",
            "prehisle",
            "progear",
            "psychic5",
            "puchicar",
            "pulstar",
            "punchout",
            "puyopuy2",
            "uopoko",
            "qbert",
            "qix",
            "raiden",
            "rdft2u",
            "rfjetu",
            "rallybik",
            "rallyx",
            "rampart",
            "rastan",
            "rbff2",
            "roadblst",
            "robotron",
            "rtype",
            "rtype2",
            "rtypeleo",
            "samuraia",
            "samsho",
            "samsho2",
            "sfrush",
            "shollow",
            "seawolf",
            "sengoku3",
            "shadoww",
            "shinobi",
            "shocktro",
            "smashtv",
            "snowbros",
            "socbrawl",
            "sokyugrt",
            "soulclbr",
            "souledgeaa",
            "spacduel",
            "sicv",
            "spyhunt",
            "starcas",
            "starw",
            "stargate",
            "sf",
            "sf2ce",
            "sf2hf",
            "sf2",
            "sfiii3",
            "strider",
            "s1945ii",
            "s1945iii",
            "srmdb",
            "sbrkout",
            "spf2t",
            "tapper",
            "tehkanwc",
            "tekken2",
            "tektagt",
            "tempest",
            "term2",
            "atetrisa",
            "tgm2p",
            "kod",
            "kof98",
            "lastblad",
            "lastbld2",
            "superspy",
            "3wonders",
            "timber",
            "timeplt",
            "trackfld",
            "trog",
            "tron",
            "truxton2",
            "umk3",
            "uccops",
            "vsav2",
            "vsav",
            "vanguard",
            "vendetta",
            "viewpoin",
            "vigilant",
            "viostorm",
            "vr",
            "wakuwak7",
            "wardner",
            "warlords",
            "wof",
            "moomesa",
            "willow",
            "wjammers",
            "wow",
            "wboy",
            "wwfwfest",
            "wwfmania",
            "xevious",
            "xmen",
            "zaxxon"]
}