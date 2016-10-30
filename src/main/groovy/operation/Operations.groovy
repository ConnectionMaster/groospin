package operation

import org.hs5tb.groospin.base.HyperSpin
import org.hs5tb.groospin.checker.handlers.XmlRom
import org.hs5tb.groospin.checker.result.CheckRomResult

/**
 * Created by Alberto on 01-Sep-16.
 */
abstract class Operations {
    HyperSpin hyperSpin
    boolean verbose = true
    boolean simulation = true

    Operations(String hyperSpin) {
        this(new HyperSpin(hyperSpin))
    }
    Operations(HyperSpin hyperSpin) {
        this.hyperSpin = hyperSpin
    }

    void log(String log) {
        if (verbose) println(log)
    }

    static Closure IS_CLONE = { CheckRomResult checkRomResult, XmlRom romNode -> romNode.cloneof }
    static Closure MISSING = { CheckRomResult checkRomResult, XmlRom romNode -> !checkRomResult.rom.exeFileFound }
    static Closure EXISTS = { CheckRomResult checkRomResult, XmlRom romNode -> checkRomResult.rom.exeFileFound }

    static Closure NO_VIDEO = { CheckRomResult checkRomResult, XmlRom romNode -> !checkRomResult.videos }
    static Closure WITH_VIDEO = { CheckRomResult checkRomResult, XmlRom romNode -> checkRomResult.videos }

    static Closure NO_WHEEL = { CheckRomResult checkRomResult, XmlRom romNode -> !checkRomResult.wheels }
    static Closure WITH_WHEEL = { CheckRomResult checkRomResult, XmlRom romNode -> checkRomResult.wheels }

    static Closure NO_THEME = { CheckRomResult checkRomResult, XmlRom romNode -> !checkRomResult.themes }
    static Closure WITH_THEME = { CheckRomResult checkRomResult, XmlRom romNode -> checkRomResult.themes }

    protected boolean none(List<Closure> conditions, CheckRomResult checkRomResult, XmlRom romNode = null) {
        !any(conditions, checkRomResult, romNode)
    }

    protected boolean any(List<Closure> conditions, CheckRomResult checkRomResult, XmlRom romNode = null) {
        return conditions.any { Closure condition ->
            if (condition.maximumNumberOfParameters == 2) {
                return condition.call(checkRomResult, romNode)
            } else if (condition.maximumNumberOfParameters == 1) {
                return condition.call(checkRomResult)
            } else {
                throw new IllegalArgumentException("Condition must have one or two parameters")
            }
        }
    }

}