package mapping

import org.hs5tb.groospin.base.HyperSpin
import org.hs5tb.groospin.base.J2K
import org.hs5tb.groospin.base.MameIni
import org.hs5tb.groospin.common.IniFile

HyperSpin hs = new HyperSpin("D:/Games/RocketLauncher")
int joystickStartPosition = 1
int player1 = joystickStartPosition
int player2 = joystickStartPosition + 1

/*
Vaciamos todos los mapeos de JoyToKey
 */
ResetAllMappings.emptyAllJoyToKeyProfiles(hs)

// Configuramos el menu de HyperSpin con teclas
ResetAllMappings.resetHyperSpinMainMenuControls(hs)

println "JoyToKey HyperSpin: Configuring profile for 360"
new J2K(hs, "HyperSpin").presets.with {
    dPadToCursor(player1)
    dPadToCursor(player2)
    analogToCursor(player1)
    analogToCursor(player2)
    xbox360Esc(player1)
    xbox360Esc(player2)
    Map mapping = [
            (XBOX360_A): RETURN,
            (XBOX360_B): ESC,
            (XBOX360_X): KEY_F,
            (XBOX360_Y): KEY_G,
            (XBOX360_BACK): F5,  // GENERO
            (XBOX360_START): KEY_H,
            (XBOX360_LB): PAGEDOWN,
            (XBOX360_LT_ANALOG): F3,   // SEARCH
            (XBOX360_RB): PAGEUP,
            (XBOX360_RT_ANALOG): F4 // FAVORITES
    ]
    buttonsTo(player1, mapping)
    buttonsTo(player2, mapping)
    save()
}



// Mapear en JoyToKey la tecla ESCAPE con BACK+START (Xbox 360) en TODOS los sistemas
println "JoyToKey all: BACK+START -> ESC....."
hs.listAllJoyToKeyProfiles().each { J2K j2k ->
    j2k.presets.with {
        xbox360Esc(player1)
        xbox360Esc(player2)
        save()
    }
}

// Los sistemas RetroArch ya funcionan con los mandos de 360 con la configuración por defecto.
// Para dejar la configuración por defecto: podemos borrar el retroarch.cfg o
// ejecutar el script ConfigRetroarch que se encarga de borrar los comandos de JoyStick de los dos players y, además,
// de configurar teclas (también borra todas las acciones de sistema que haya configurados para que funcione todo
// por defecto)
ResetAllMappings.resetRetroArch(hs.retroArch)

println "JoyToKey RetroArch: Configuring 360 BACK+RB = F1"
// Después, se mapea en JoyToKey la tecla F1 con BACK+RB
hs.listSystemsRetroArch()*.loadJ2KConfig().each { J2K j2k ->
    j2k.presets.with {
        xbox360RetroArchF1(player1)
        xbox360RetroArchF1(player2)
        save()
    }
}

/* Los sistemas MAME ya funcionan con los mandos de 360 si estan conectados como JOYSTICKS 1 Y 2

¿Porque se usa entonces JoyToKey?
- El dpad digital del mando de 360 no funciona, asi que se mapea a los cursores (solo funciona el analogico de la izquierda)
- Se mapean BACK y START para echar moneda y start
TODAS ESTAS CONFIGURACIONES SE PODRIAN HACER MODIFICANDO EL default.cfg pero hay un problema:

SI SE DESENCHUFA EL MANDO (O SI ES INALAMBRICO Y SE APAGA) CUALQUIER CONFIGURACIÓN QUE SE TENGA ECHA EN EL default.cfg
SE BORRA. Por lo tanto, cuando se usan mandos que se pueden enchufar y desenchufar, lo mejor es no editar el default.cfg
y hacer el mapeo en el JoyToKey.
 */

// Se elimina el default.cfg para que se vuelva a generar vacio, haciendo antes una copia de seguridad
ResetAllMappings.resetMameCtrl(hs)

println "JoyToKey MAME: Configuring 360 additional buttons (coin, start, dpad): ${(hs.listSystemsMAME()+hs.getSystem("HBMAME"))*.name}"
// Mapeos en JoyToKey
(hs.listSystemsMAME()+hs.getSystem("HBMAME"))*.loadJ2KConfig().each { J2K j2k ->
    j2k.presets.with {
        xbox360MameTab(player1)
        xbox360MameTab(player2)
        dPadToCursor(player1)
        dPadTo(player2, KEY_D, KEY_F, KEY_R, KEY_G)
        buttonsTo(player1, [
                (XBOX360_BACK): KEY_5,
                (XBOX360_START): KEY_1
        ])
        buttonsTo(player2, [
                (XBOX360_BACK): KEY_6,
                (XBOX360_START): KEY_2
        ])
        save()
    }
}


println "JoyToKey Future Pinball. (RUN THE REG FILE!!!!!!!!!!!)"
// Future Pinball. Funciona mejor con teclado. Cargar el registro de Windows para que se carguen estas teclas
hs.getSystem("Future Pinball").loadJ2KConfig().presets.with {
    analogLeftTo(player1, F1, F4, F3, F2)  // vistas
    analogRightTo(player1, F5, F8, F7, F6)  // vistas
    dPadTo(player1, KEY_F, RETURN, SPACE, KEY_A)  // abajo sacar, resto golpear
    buttonsTo(player1, [
            (XBOX360_A): RETURN, // sacar
            (XBOX360_Y): TAB, // mirar arriba
            (XBOX360_BACK): KEY_5, // start
            (XBOX360_START): KEY_1, // moneda
            (XBOX360_LB): [KEY_Z, KEY_X],  // pinballs izquierdo
            (XBOX360_RB): [KEY_N, KEY_M],  // pinballs derecho
    ])
    save()
}

/*
Pinball FX2. Ya funciona con Xbox 360 directamente
*/

/*
Pinball Arcade. Ya funciona con Xbox directamente
HKEY_CURRENT_USER\Software\PinballArcade\PinballArcade
 */

/*
Sony PlayStation 2
Configuación en:
d:\Games\Emulators\PCSX2\PCXS2.gigapig\inis\LilyPad.ini
Se supone ya está configurado para 360
 */

println "JoyTokey AAE"
// AAE funciona mejor con teclado
hs.getSystem("AAE").loadJ2KConfig().presets.with {
    dPadToCursor(player1)
    dPadToCursor(player2)
    analogToCursor(player1)
    analogToCursor(player2)
    buttonsTo(player1, [
            (XBOX360_A): ALT,
            (XBOX360_B): CTRL,
            (XBOX360_X): SHIFT,
            (XBOX360_Y): SPACE,
            (XBOX360_BACK): KEY_5,
            (XBOX360_START): KEY_1
    ])
    buttonsTo(player2, [
            (XBOX360_A): ALT,
            (XBOX360_B): CTRL,
            (XBOX360_X): SHIFT,
            (XBOX360_Y): SPACE,
            (XBOX360_BACK): KEY_5,
            (XBOX360_START): KEY_2
    ])
    save()
}
ResetAllMappings.resetWinVice(hs)
println "JoyToKey WinVICE: configuring ${hs.listSystemsWinVICE()*.name}"
hs.listSystemsWinVICE()*.loadJ2KConfig().with { J2K j2k ->
    j2k.presets.with {
        Map mapping = [
                (XBOX360_B)        : SPACE,
                (XBOX360_X)        : ENTER,
                (XBOX360_Y)        : TAB,
                (XBOX360_BACK)     : F1,
                (XBOX360_START)    : CAPSLOCK,  // RUN/STOP
                (XBOX360_LB)       : KEY_N,
                (XBOX360_LT_ANALOG): KEY_1,
                (XBOX360_RB)       : KEY_Y,
                (XBOX360_RT_ANALOG): KEY_2,
        ]
        buttonsTo(player1, mapping)
        buttonToKey(player1, XBOX360_A, KEY_Q)

        buttonsTo(player2, mapping)
        buttonToKey(player2, XBOX360_A, KEY_U)

        dPadTo  (player1, KEY_A, KEY_S, KEY_W, KEY_D)
        analogToCursor(player1)

        dPadTo  (player2, KEY_J, KEY_K, KEY_I, KEY_L)
        analogToCursor(player2)
        save()
    }
}

ResetAllMappings.resetPS2Keys(hs)
ResetAllMappings.resetPPSSPP(hs)

/*
DONE:
AAE: [AAE]

RetroArch: [Atari 2600, Atari 7800, Atari 2600 - Arcadia Supercharger, Atari Jaguar, Atari Lynx, Bandai WonderSwan, Bandai WonderSwan Color, NEC PC Engine, NEC PC Engine-CD, NEC PC-FX, NEC SuperGrafx, NEC TurboGrafx-16, NEC TurboGrafx-CD, Nintendo 64, Nintendo DS, Nintendo Entertainment System, Super Nintendo Entertainment System, Nintendo Famicom, Nintendo Famicom Disk System, Nintendo Game & Watch, Nintendo Game Boy, Nintendo Game Boy Advance, Nintendo Game Boy Color, Nintendo Satellaview, Nintendo Sufami Turbo, Nintendo Super Famicom, Nintendo Virtual Boy, Sega 32X, Sega Genesis, Sega Game Gear, Sega Mark III, Sega Master System, Sega Mega Drive, Sega Nomad, Sega Pico, Sega Saturn, Sega Saturn Japan, Sega SG-1000, SNK Neo Geo Pocket, SNK Neo Geo Pocket Color, Sony PlayStation]
RetroArch Extended: [Nintendo 64 Japan, Nintendo 64 Europe, Nintendo Entertainment System Europe, Nintendo Entertainment System Asia, Super Nintendo Entertainment System Europe, Super Nintendo Entertainment System Japan, Sega Master System Japan, Sega Mega Drive Europe]

MAME: [Atari Classics, Capcom Classics, Capcom Play System, Capcom Play System II, Capcom Play System III, Data East Classics, HyperNeoGeo64, Irem Classics, Kaneko, Konami Classics, Atlus, Banpresto, Cave, Gaelco MAME, Bally, Sammy, Nichibutsu, Seibu Kaihatsu, Jaleco, Mature MAME, Best of MAME, MAME, Tecmo, Toaplan, Mitchell Corporation, Visco, SNK Classics, MAME 4 Players, Midway Classics, Namco Classics, Namco System 22, Nintendo Classics, Psikyo, Sega Classics, Sega ST-V, Shotgun Games, SNK Neo Geo AES, Technos Classics, Taito Classics, Trackball Games, Williams Classics]
HBMAME: [HBMAME]

WinVICE: [Commodore 64]

Future Pinball: [Future Pinball]
Pinball FX2: [Pinball FX2]
Pinball Arcade DX11: [Pinball Arcade]

PPSSPP: [Sony PSP, Sony PSP Minis]
PCSX2: [Sega Ages, Sony PlayStation 2]

PENDING:
Dolphin5: [Nintendo Wii, Nintendo WiiWare]



Sega Model 2 Emulator: [Sega Model 2]
SuperModel: [Sega Model 3]
Demul70: [Cave 3rd, Gaelco, Sammy Atomiswave, Sega Hikaru, Sega Naomi]
NullDC: [Sega Dreamcast]
Project64 DD: [Nintendo 64DD]
Dolphin GC: [Nintendo GameCube]
PokeMini: [Nintendo Pokemon Mini]
Fusion: [Sega CD, Sega Mega-CD, Sega SC-3000]
FourDO: [Panasonic 3DO]
CPCE: [Amstrad CPC]
Sinclair ZX Spectrum
Daphne: [Daphne]

Spectaculator: [Sinclair ZX Spectrum]
MESS: [Amstrad GX4000, Atari 5200, Bally Astrocade, Casio PV-1000, Casio PV-2000, ColecoVision, Creatronic Mega Duck, Emerson Arcadia 2001, Entex Adventure Vision, Epoch Game Pocket Computer, Epoch Super Cassette Vision, Exidy Sorcerer, Fairchild Channel F, Funtech Super Acan, GCE Vectrex, Interton VC 4000, Magnavox Odyssey 2, Mattel Intellivision, Nintendo Super Game Boy, Philips CD-i, RCA Studio II, Sony Pocketstation, Texas Instruments TI 99-4A, Tiger Game.com, VTech CreatiVision, Watara Supervision, Aamber Pegasus, Hartung Game Master]
MESScart1: [Sord M5]
MESSApogee: [Apogee BK-01, Sega VMU, Vector-06C, VTech Socrates, Casio Loopy]
BeebEm: [Acorn BBC Micro]
Universal Emualator Alf: [ALF TV Game]
PCLauncher: [American Laser Games, Big Fish Games, Doujin Soft, Flash Games, Lucasarts Adventure Games, Locomalito Games, Nintendo Game and Watch, Party Games, PC Games, Taito Type X, TouchGames, Touhou Project, Pack Remasterizados]
AppleWin: [Apple II]
Atari800: [Atari 8-bit, Atari XEGS]
Project Tempest: [Atari Jaguar CD]
Hatari: [Atari ST]
WinUAE: [Commodore Amiga, Commodore Amiga CD32, Commodore CDTV]
DICE: [DICE]
MFME v10.1a: [Fruit Machine]
UNZ: [Fujitsu FM Towns]
GeePee32: [GamePark 32]
ZiNc: [Zinc]


BlueMSX: [Microsoft MSX, Microsoft MSX2, Microsoft MSX2+]
OpenMSXPalm: [MSX Palcom Laserdisc]
MUGEN: [MUGEN]
SimCoupe: [MGT Sam Coupe]
Citra: [Nintendo 3DS]
CEMU162: [Nintendo Wii U]
OpenBOR: [OpenBOR]
DCVG5K: [Philips VG 5000]
Casual Games: [PopCap]
Dolphin Triforce: [Sega Triforce]
Xmillennium: [Sharp X1]
XM6: [Sharp X68000]
NeoRaine: [SNK Neo Geo CD]
Nestopia: [Technos]
BlueMSX Zemmix: [Zemmix, Zemmix Neo]


ScummVM: [ScummVM]
Daphen WoW Action Max: [WoW Action Max]
Visual Pinball 9: [Visual Pinball]
DFend: [Microsoft MS-DOS]
DOSBox eXoDOS: [Microsoft MS-DOS eXoDOS]
DOSBox Win3XO: [Microsoft Windows 3.x]
video dummy: [Vintage Commercials]
 */