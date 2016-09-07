package org.hs5tb.groospin.base

import groovy.xml.MarkupBuilder
import org.hs5tb.groospin.common.StringHelper

/**
 * Created by Alberto on 01-Sep-16.
 */
class HyperSpinDatabase {

    String listname
    String lastlistupdate
    String listversion
    String exporterversion
    List<Rom> roms
    File db

    HyperSpinDatabase load(File db, Closure filter = null) {
        this.db = db
        Node menu = new XmlParser().parse(db.newReader())
        listname = menu.listname.text()
        lastlistupdate = menu.lastlistupdate.text()
        listversion = menu.listversion.text()
        exporterversion = menu.exporterversion.text()

        roms = loadRoms(menu, filter)
        return this
    }

    HyperSpinDatabase splitAndWriteGenres(String folder, Map headers = this.properties) {
        splitAndWriteGenres(folder, { Rom rom -> rom.genre }, headers)
        return this
    }

    HyperSpinDatabase splitAndWriteGenres(String folder, Closure condition, Map headers = this.properties) {
        writeGenres(roms.groupBy(condition), new File(folder), headers)
        return this
    }

    HyperSpinDatabase export(File file, Map headers = this.properties) {
        write(roms, file, headers)
        return this
    }

    static void writeGenres(Map<String, List<Rom>> romsByGenre, String folder, Map headers = [:]) {
        writeGenres(romsByGenre, new File(folder), headers)
    }

    static void writeGenres(Map<String, List<Rom>> romsByGenre, File folder, Map headers = [:]) {
        Map<String, List<Rom>> romsByGenreClean = new LinkedHashMap<>()

        romsByGenre.each { Map.Entry<String, List<Rom>> entry ->
            if (entry.value) {
                // Clean the empty genres, if any
                romsByGenreClean[entry.key?.trim()?:"Unknown"] = entry.value
            }
        }
        if (!romsByGenreClean || romsByGenreClean.size() == 1) {
            // Only write genres if we have more then one...
            return
        }
        StringBuffer genreContent = new StringBuffer()
        romsByGenreClean.keySet().sort().each {
            genreContent << "  <game name=\"${it}\"/>\n"
        }
        if (!folder.exists()) folder.mkdirs()
        new File(folder, "genre.xml").text = "<menu>\n${genreContent}</menu>"

        romsByGenreClean.each { String genre, List<Rom> genreRoms ->
            write(genreRoms, new File(folder, "${StringHelper.unescapeHTML(genre)}.xml"), headers)
        }
    }

    static void fixDatabase(File db, File dst = null, Closure genreCondition = null) {
        HyperSpinDatabase hyperSpinDatabase = new HyperSpinDatabase()
        hyperSpinDatabase.load(db)
        fixDatabase(hyperSpinDatabase, dst, genreCondition)
    }

    static void fixDatabase(HyperSpinDatabase hyperSpinDatabase, File dst = null, Closure genreCondition = null) {
        if (!dst) dst = hyperSpinDatabase.db.parentFile
        hyperSpinDatabase.export(new File(dst, hyperSpinDatabase.db.name))
        if (genreCondition) {
            hyperSpinDatabase.splitAndWriteGenres(dst as String, genreCondition)
        }
    }

    static List<String> listRomNames(File db) {
        new XmlParser().parse(db).game.collect { it.@name }
    }

    static List<Rom> loadRoms(File db, Closure filter = null) {
        Node menu = new XmlParser().parseText(db.text)
        return loadRoms(menu)
    }

    static List<Rom> loadRoms(Node menu, Closure filter = null) {
        List<Rom> roms = []
        menu.game.each { Node node ->
            Rom rom = new Rom().loadFromHyperSpinDatabase(node)
            if (!filter || filter(rom)) {
                roms << rom
            }
        }
        return roms
    }

    static void write(List<Rom> roms, String file, Map header = [:]) {
        write(roms, new File(file).newWriter(), header)
    }
    static void write(List<Rom> roms, File file, Map header = [:]) {
        file.parentFile.mkdirs()
        write(roms, file.newWriter(), header)
    }

    static void write(List<Rom> roms, Writer writer, Map headers = [:]) {
        StringBuilder lettersAlreadyUsed = new StringBuilder()
        MarkupBuilder mb = new MarkupBuilder(writer)
        mb.mkp.xmlDeclaration(version: "1.0", encoding: "utf-8")
        mb.menu {
            setOmitNullAttributes(true)
            setDoubleQuotes(true)
            header {
                if (headers.listname) listname(headers.listname)
                if (headers.listversion) listversion(headers.listversion)
                if (headers.lastlistupdate) lastlistupdate(headers.lastlistupdate)
                if (headers.exporterversion) exporterversion(headers.exporterversion)
            }
            roms.sort { it.description }.each { Rom rom ->
                String image = getLetter(lettersAlreadyUsed, rom)
                game(name: rom.name, index: image ? "true" : "", image: image?.toLowerCase() ?: "") {
                    description(rom.description)
                    cloneof(rom.cloneof) // clone of is mandatory, even if it's empty! HS fails if you use parents_only=true and there is no <cloneof> tag!
                    if (rom.crc) crc(rom.crc)
                    if (rom.manufacturer) manufacturer(rom.manufacturer)
                    if (rom.year) year(rom.year)
                    if (rom.genre) genre(rom.genre)
                    if (rom.rating) rating(rom.rating)
                    if (rom.enabled) enabled(rom.enabled)
                    if (rom.exe) exe(rom.exe)
                }
            }
        }
    }

    private static String getLetter(StringBuilder lettersAlreadyUsed, Rom rom) {
        String image
        if (!rom.cloneof) {
            // Only the parents have letter because if you use the parents_only=true flag and
            // choose a letter which belongs to a clone, HS will jump to the beginning
            image = fixLetter(rom.description.toUpperCase().charAt(0))
            if (lettersAlreadyUsed.contains(image)) {
                image = null // Si ya estaba la letra, no la usamos
            } else {
                lettersAlreadyUsed << image // la marcamos como añadida para la proxima
            }
        }
        return image
    }

    static private String fixLetter(char letter) {
        String image = letter
        if (letter < (char) '0') {
            image = '0'
        } else if (letter > ((char) '9') && letter < ((char) 'A')) {
            image = '9'
        } else if (letter > ((char) 'Z')) {
            image = 'Z'
        }
        return image
    }

}
