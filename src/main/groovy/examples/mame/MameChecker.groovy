package examples.mame

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.hs5tb.groospin.base.MameMachine
import org.hs5tb.groospin.common.IOTools

import java.util.zip.ZipEntry
import java.util.zip.ZipFile


class MameChecker {
    Map<String, MameMachine> index = [:]
    List<MameMachine> games = []
    List<File> romFolders
    boolean checkWarnings = false
    boolean verbose = true
    boolean checkSHA = false

    void log(String s) {
        if (verbose) println s
    }

    MameChecker loadDat(String datFileName, Closure filter = null) {
        index = [:]
        games = []
        long start = System.currentTimeMillis()
        MameMachine.loadRoms(new File(datFileName), false, null).each { MameMachine rom ->
            index[rom.name.toLowerCase()] = rom
            if (!filter || filter.call(rom)) {
                games << rom
            }
        }
        log "Parsing dat time: ${(System.currentTimeMillis()-start)/1000}s (${games.size()} machines)"
        return this
    }

    static class Report {
        Map<String, List> filesByRom
        Set<String> missing
        Set<File> files
    }

    Report checkRoms(List romFolderString) {
        Set missing = new LinkedHashSet()
        Map<String, List> filesByRom = [:]
        romFolders = romFolderString.collect { it instanceof String ? new File(it) : it }
        long start = System.currentTimeMillis()
        games.each { MameMachine mameMachine ->
            List filesChecked = []
            List filesUsed = []
            Map romsPending = extractAllRomsToCheck(mameMachine)
            checkRomsAndRemoveOk(romsPending, filesChecked, filesUsed, "", mameMachine.name)
            if (romsPending) {
                if (mameMachine.cloneof) {
                    MameMachine parent = index[mameMachine.cloneof.toLowerCase()]
                    if (parent) {
                        checkRomsAndRemoveOk(romsPending, filesChecked, filesUsed, mameMachine.name, parent.name)
                        if (romsPending) {
                            if (parent.romof) {
                                MameMachine bios = index[parent.romof.toLowerCase()]
                                if (bios) {
                                    checkRomsAndRemoveOk(romsPending, filesChecked, filesUsed, "", bios.name)
                                }
                            }
                        }
                    }
                } else if (mameMachine.romof) {
                    MameMachine bios = index[mameMachine.romof.toLowerCase()]
                    if (bios) {
                        checkRomsAndRemoveOk(romsPending, filesChecked, filesUsed, "", bios.name)
                    }
                }
            }
            if (romsPending) {
                log "ERROR! Missing files for rom \"${mameMachine.name}\":${romsPending.keySet().join(", ")}. Places checked: ${filesChecked}"
                missing << mameMachine.name
            } else {
                // log "${mameMachine.name} ok!"
            }

            filesChecked = []
            Map chdsPending = extractAllDisksToCheck(mameMachine)
            checkChdsAndRemoveOk(chdsPending, filesChecked, filesUsed, mameMachine.name)

            if (chdsPending) {
                if (mameMachine.cloneof) {
                    MameMachine parent = index[mameMachine.cloneof.toLowerCase()]
                    if (parent) {
                        checkChdsAndRemoveOk(chdsPending, filesChecked, filesUsed, parent.name)
                        if (chdsPending) {
                            if (parent.romof) {
                                MameMachine bios = index[parent.romof.toLowerCase()]
                                if (bios) {
                                    checkChdsAndRemoveOk(chdsPending, filesChecked, filesUsed, bios.name)
                                }
                            }
                        }
                    }
                } else if (mameMachine.romof) {
                    MameMachine bios = index[mameMachine.romof.toLowerCase()]
                    if (bios) {
                        checkChdsAndRemoveOk(chdsPending, filesChecked, filesUsed, bios.name)
                    }
                }
            }
            if (chdsPending) {
                log "ERROR! Missing chds \"${mameMachine.name}\": ${chdsPending.keySet().join(", ")}. Places checked: ${filesChecked}"
            } else {
                // log "${mameMachine.name} ok!"
            }
            if (chdsPending || romsPending) {
                missing << mameMachine.name
            } else {
                filesByRom[mameMachine.name] = filesUsed
            }
        }
        log "Checking time: ${(System.currentTimeMillis()-start)/1000}s"
        return new Report(missing: missing, filesByRom: filesByRom, files: filesByRom.values().flatten() as Set<File>)
    }

    Map checkRomsAndRemoveOk(Map pendingRomBins, List filesChecked, List filesUsed, String folderInside, String filename) {
        File zip = IOTools.findFileInFolders(romFolders, filename+".zip")
        File sevenz = IOTools.findFileInFolders(romFolders, filename+".7z")
        int currentSize = pendingRomBins.size()

        if (zip) {
            filesChecked << zip.toString()
            ZipFile zipFile = new ZipFile(zip)
            zipFile.entries().each { ZipEntry entry ->
                // log "${entry.name}"
                List<MameMachine.RomBin> romBins = findRomBinByEntryName(pendingRomBins, entry.name, folderInside)
                if (romBins) {
                    if (checkWarnings) checkWarnings(romBins[0], entry.size, entry.crc, "${zip.canonicalPath}/${entry.name}", { -> zipFile.getInputStream(entry) })
                    romBins.each { MameMachine.RomBin romBin -> pendingRomBins.remove(romBin.name.toLowerCase()) }
                }
            }
            if (pendingRomBins.size() < currentSize) {
//                filesByRom << zip.name
                filesUsed << zip
            }
        } else if (sevenz) {
            try {
                filesChecked << sevenz.toString()
                SevenZFile sevenZFile = new SevenZFile(sevenz)
                SevenZArchiveEntry entry = sevenZFile.nextEntry
                while (entry!=null){
                    List<MameMachine.RomBin> romBins = findRomBinByEntryName(pendingRomBins, entry.name, folderInside)
                    if (romBins) {
                        if (checkWarnings) checkWarnings(romBins[0], entry.size, entry.crcValue, "${sevenz.canonicalPath}/${entry.name}", {-> sevenZFile.getCurrentStream()})
                        romBins.each { MameMachine.RomBin romBin -> pendingRomBins.remove(romBin.name.toLowerCase()) }
                    }
                    entry = sevenZFile.nextEntry
                }
                sevenZFile.close();
                if (pendingRomBins.size() < currentSize) {
//                    filesByRom << sevenz.name
                    filesUsed << sevenz
                }
            } catch (e) {
                println "FATAL error reading ${sevenz}"
                e.printStackTrace()
            }
        } else {

            filesChecked << "No ${filename}.zip or ${filename}.7z found"
        }
        return pendingRomBins
    }

    void checkWarnings(MameMachine.RomBin romBin, long fileSize, long crc, String canonicalEntryName, Closure getInputStream) {
        if (romBin.size >0 && fileSize != romBin.size) {
            log "warning! bad size checking rom $canonicalEntryName}"
        } else if (Long.decode("0x"+romBin.crc) != crc) {
            if (checkSHA && romBin.sha1) {
                if (romBin.sha1 != IOTools.sha1(getInputStream.call())) {
                    log "warning! bad crc & sha1 checking rom ${canonicalEntryName}"
                }
            } else {
                log "warning? bad crc (no sha1 found) checking rom ${canonicalEntryName}"
            }
        }

    }

    List<MameMachine.RomBin> findRomBinByEntryName(Map pendingRomBins, String name, String folderInside) {
        name = name.toLowerCase()
        folderInside = folderInside.toLowerCase()
        int dash = name.indexOf("/")
        if (dash > 0) {
            String folder = name.substring(0, dash)
            if (folder != folderInside) return
            name = name - (folderInside.toLowerCase()+"/")
        }
        List matches = []
        MameMachine.RomBin romBin = pendingRomBins[name]
        if (romBin) matches << romBin
        matches.addAll(pendingRomBins.values().findAll { it.merge?.toLowerCase() == name })
        return matches
    }

    void checkChdsAndRemoveOk(Map pendingRomBins, List filesChecked, List filesUsed, String mameMachineName) {
        Set chdsFound = new HashSet()
        pendingRomBins.values().each { MameMachine.RomBin romBin ->
            File chd = IOTools.findFileInFolders(romFolders, mameMachineName+"/"+romBin.name+".chd")
            if (!chd && romBin.merge) {
                chd = IOTools.findFileInFolders(romFolders, mameMachineName+"/"+romBin.merge+".chd")
            }
            if (chd) {
                filesChecked << chd.toString()
//                filesByRom << mameMachineName+"/"+chd.name
                filesUsed << chd
                if (romBin.size >0 && chd.size() != romBin.size) {
                    log "warning! bad size checking disk ${chd.canonicalPath}"
                }
                chdsFound << romBin.name.toLowerCase()
            } else {
                filesChecked << chd.toString()+" not found"
            }
        }
        chdsFound.each { pendingRomBins.remove(it) }
    }

    private Map extractAllDisksToCheck(MameMachine mameMachine) {
        return extractPendingRomBins(mameMachine.disks)
    }

    private Map extractAllRomsToCheck(MameMachine mameMachine) {
        return extractPendingRomBins(mameMachine.roms)
    }

    private Map extractPendingRomBins(Collection romsOrDisks) {
        Map pendingRomBins = romsOrDisks.inject([:]) { Map map, MameMachine.RomBin romBin ->
            if (romBin.status != MameMachine.RomBin.Status.baddump && romBin.status != MameMachine.RomBin.Status.nodump) {
                map[romBin.name.toLowerCase()] = romBin
            }
            map
        }
        return pendingRomBins
    }

}



//ZipFile zipFile = new ZipFile(new File("c:\\Users\\Alberto\\Downloads\\mame178\\MAME ROMs 178 split\\005.zip"))
//zipFile.entries().each { ZipEntry zipEntry ->
//    println "${zipEntry.name}, size: ${zipEntry.size}, crc: ${zipEntry.crc}, sha1: ${IOTools.sha1(zipFile.getInputStream(zipEntry))}"
//}
//println "--"

//SevenZFile sevenZFile = new SevenZFile(new File("c:\\Users\\Alberto\\Downloads\\mame178\\MAME 0.178 ROMs\\mk2.7z"))
//SevenZArchiveEntry entry = sevenZFile.nextEntry
//while (entry!=null){
//    println "${entry.name}, size: ${entry.size}, crc: ${entry.crcValue}, sha1: ${IOTools.sha1(sevenZFile.getCurrentStream())}"
//    entry = sevenZFile.getNextEntry()
//}
//sevenZFile.close();
//return
