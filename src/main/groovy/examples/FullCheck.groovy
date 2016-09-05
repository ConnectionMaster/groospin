package examples

import org.hs5tb.groospin.base.*
import org.hs5tb.groospin.checker.*
import org.hs5tb.groospin.checker.handlers.*

HyperSpin hs = new HyperSpin(
        "D:/Games/Hyperspin-fe",
        "D:/Games/RocketLauncher")

def systems = []

String reportRoot = "D:/Games/Soft/Groospin/generated/reports/${new Date().format("yyyy-MM-dd HH-mm")}"


new Checker(hs).
        addHandler(new HumanInfo("${reportRoot}/human-report.txt", true)).
        addHandler(new HaveHtmlList("${reportRoot}/all.html", true)).
        addHandler(new HaveHtmlList("${reportRoot}/have-list.html", false)).
        addHandler(new MissingTxtList("${reportRoot}/missing.csv", ";")).
        addHandler(new AllRomsCsvList("${reportRoot}/roms.csv", ";")).
        addHandler(new SystemCsvList("${reportRoot}/systems.csv", ";")).
        checkSystems()



