package com.cazapp.model

enum class Note (val label : String ,val alt_label : String ){
    DO("do","do"),
    DO_("do#","reb"),
    RE("re","re"),
    RE_("re#","mib"),
    MI("mi","mi"),
    FA("fa","fa"),
    FA_("fa#","solb"),
    SOL("sol","sol"),
    SOL_("sol#","lab"),
    LA("la","la"),
    LA_("la#","sib"),
    SI("si","si")
}

class Line (var line : Int, var type : String, var content : String) {

    constructor():this(0, "","")

    fun transpose(semitones : Int){
        if (type != "acorde"){
            return
        }

        // If exist whitespaces at the beginning
        // ^\s*
        val seq_whites = Regex("^\\s*").findAll(content)
        var whites = ""
        for (seq_white in seq_whites) {
            whites += seq_white.value
        }

        // Get chords with spaces in a row
        // [a-zA-Z0-9#]+\s*
        val seqChords = Regex("[a-zA-Z0-9#]+\\s*").findAll(content)
        var newChords = mutableListOf<String>()
        for (chord in seqChords)
        {
            val notes = Note.values()
            val ordinal = checkNote(chord.value,notes)
            if (ordinal!=-1){
                var newIndex = (notes[ordinal].ordinal + semitones)
                if (newIndex >= 0) {
                    newIndex %= notes.size
                }else{
                    newIndex += notes.size
                }
                var newNote = notes[newIndex].label
                var toMatch = notes[ordinal].label

                if (chord.value.contains(Regex(notes[ordinal].alt_label,RegexOption.IGNORE_CASE))){
                    toMatch = notes[ordinal].alt_label
                }

                if (chord.value[0].isUpperCase()){
                    newNote = newNote.toUpperCase()
                }

                newChords.add(Regex(toMatch,RegexOption.IGNORE_CASE).replace(chord.value,newNote))

            }

        }

        // join first whitespaces and chords
        var newContent = whites
        for (newChord in newChords){
            newContent += newChord
        }

        content = newContent
    }

    private fun checkNote(sNote : String, notes : Array<Note>) : Int {

        //Hallar máximo longitud de cadena para comparar
        val min_longitud = 2
        var max_longitud = sNote.length
        if (max_longitud > 4){
            max_longitud = 4
        }

        for (long in max_longitud downTo min_longitud){
            val firstAttempt = sNote.substring(0, long)
            for (note in notes) {
                if (firstAttempt.equals(note.label, ignoreCase = true) || firstAttempt.equals(
                        note.alt_label,
                        ignoreCase = true
                    )
                ) {
                    return note.ordinal
                }
            }
        }

        return -1
    }
}