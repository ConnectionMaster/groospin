package examples

import operation.Operations
import org.hs5tb.groospin.base.HyperSpin

List systems = ["Sega Mega Drive"]

new Operations(new HyperSpin(
        "D:/Games/HyperSpin-fe",
        "D:/Games/RocketLauncher")).extractFromDatabase("-missing", [Operations.MISSING], systems)