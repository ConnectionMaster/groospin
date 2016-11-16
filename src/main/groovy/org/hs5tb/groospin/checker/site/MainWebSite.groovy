package org.hs5tb.groospin.checker.site

import org.hs5tb.groospin.base.RLSystem
import org.hs5tb.groospin.checker.BaseCheckHandler
import org.hs5tb.groospin.checker.result.CheckRomResult
import org.hs5tb.groospin.checker.result.CheckTotalResult
import org.hs5tb.groospin.common.FileBuffer

import static org.hs5tb.groospin.common.IOTools.humanReadableByteSize
import static org.hs5tb.groospin.common.IOTools.sanitize

/**
 * Created by Alberto on 12-Jun-16.
 */
class MainWebSite extends BaseCheckHandler {
    private File root
    private FileBuffer websiteSystems
    boolean includeMissing = false
    private n = 0

    @Override
    boolean needsRomFolderSize() {
        true
    }

    @Override
    boolean needsMediaFolderSize() {
        true
    }

    MainWebSite(String root, boolean includeMissing) {
        this(new File(root), includeMissing)
    }

    MainWebSite(File root, boolean includeMissing) {
        println ("MainWebSite root: "+root)
        this.includeMissing = includeMissing
        this.root = root
        this.websiteSystems = new FileBuffer(new File(root, "systems.html"))
    }

    @Override
    void startCheck() {

        websiteSystems << """
<style>
table#systems {
    font: 11pt Arial;
    border-spacing: 0;
    border-collapse: collapse;
    border:2px solid #ccc ;
    table-layout: auto;
}
table#systems thead td {
    padding: 3px;
    background:#000000;
    color:white;
    font: bold 11pt Arial;
}
table#systems tbody td, table#systems tfoot td {
    border-bottom: 1px solid #e5e5e5 ;
    padding: 3px;
}
tfoot td, td.n {
    background:#e5e5e5;
}
table#systems tbody td .emu {
    font-size: 8pt;
}
td.system, td.roms, td.romSize{
    font: bold 11pt Arial;
}
td.group {
    font: bold 14pt/30pt Arial;
}
table tfoot td {
    border-top:2px solid #ccc ;
}
table#systems tbody td.instable {
    font: normal 9pt arial;
    color: #F00;
    xxbackground: #ffd966;
    text-align:center;
}
table#systems tbody td.ok {
    font: normal 9pt arial;
    text-align:center;
}
table#systems tbody td.perfect {
    xxxbackground: #93c47d;
    text-align:center;
}
table#systems tbody td.state {
}
.toolttip {
    position: relative;
    display: inline-block;
}

.toolttip .toolttiptext {
    visibility: hidden;
    width: 200px;
    background-color: #666;
    color: #fff;

    border:0;
    text-align: center;
    border-radius: 6px;
    padding: 10px;

    /* Position the toolttip */
    position: absolute;
    z-index: 1;
    bottom: 100%;
    left: 50%;
    margin-left: -60px;
    opacity: 0;
    transition: opacity 0.5s;
}

.toolttip:hover .toolttiptext {
    visibility: visible;
    opacity: 1;
}
</style>
"""

        websiteSystems << "<table id='systems'><thead>\n<tr>\n    <td>#</td><td colspan='2'>Sistema</td><td colspan='2'>Estado</td><td>Roms</td><td>Tamaño</td><td>Wheels</td><td>Video</td><td>Temas</td><td>Tamaño medias</td>\n</tr>\n</thead>\n<tbody>"
    }

    SystemWebSite haveHtmlList
    @Override
    void startSystem(RLSystem system) {
        haveHtmlList = new SystemWebSite(new File(root, "system-${system.name}.html"), includeMissing)
        haveHtmlList.startSystem(system)
    }

    @Override
    void romChecked(CheckRomResult checkResult) {
        haveHtmlList.romChecked(checkResult)
    }

    @Override
    void startGroup(String groupName) {
        websiteSystems << "<tr>\n    <td></td><td class='group' colspan='10'>${groupName}</td>\n</tr>\n</thead>\n<tbody>"
    }

    String baseImg = "../static/icons/"

    String arcadeIcon    = "<span class='toolttip'><img src='${baseImg}arcade-flat-icon.png' width='14'/><span class=\"toolttiptext\">Ideal para recreativa</span></span>"
    String excellentIcon = "<span class='toolttip'><img src='${baseImg}icon-certified.png' width='20'/><span class=\"toolttiptext\">¡Emulación excelente!</span></span>"
    String dangerIcon    = "<span class='toolttip'><img src='${baseImg}danger-icon.png' width='22'/><span class=\"toolttiptext\">El sistema es inestable, no está bien emulado o presenta dificultades para jugar</span></span>"
    @Override
    void endSystem(CheckTotalResult checkResult) {
        haveHtmlList.endSystem(checkResult)
        websiteSystems << "<tr>\n    <td class='n'>${++n}</td><td><img src='${baseImg}${checkResult.system.name}.png' onerror=\"this.style.display='none'\"/></td><td class='system'><a href='/sistemas/${sanitize(checkResult.system.name)}/'>${checkResult.system.name}</a><div class='emu'>${checkResult.system.defaultEmulator?.name ?: ""}</div></td>"
        websiteSystems << "<td class='${systemConfig.arcade?'arcadeYes':'arcadeNo'}'>${systemConfig.arcade?arcadeIcon:""}</td><td class='state ${systemConfig.perfect ? "perfect" : !systemConfig.stable ? "instable" : ""}'>${systemConfig.perfect ? excellentIcon : !systemConfig.stable ? dangerIcon : ""}</td><td class='roms'>${checkResult.totalRoms}</td><td class='romSize'>${humanReadableByteSize(checkResult.totalRomSize)}</td>" +
                "<td class='wheels'>${checkResult.wheels}</td><td class='videos'>${checkResult.videos}</td><td class='themes'>${checkResult.themes}</td><td class='mediaSize'>${humanReadableByteSize(checkResult.totalMediaSize)}</td></tr>"
    }

    @Override
    void endCheck(CheckTotalResult checkResult) {
        websiteSystems << "</tbody>\n<tfoot>\n<tr>\n    <td></td><td colspan='4' class='total'>Total ${n} sistemas</td><td class='roms'>${checkResult.totalRoms}</td><td class='romSize'>${humanReadableByteSize(checkResult.totalRomSize)}</td>" +
                "<td class='wheels'>${checkResult.wheels}</td><td class='videos'>${checkResult.videos}</td><td class='themes'>${checkResult.themes}</td><td class='mediaSize'>${humanReadableByteSize(checkResult.totalMediaSize)}</td></tr>"
        websiteSystems << "</tfoot>\n</table>"
        websiteSystems.flush()
    }
}
