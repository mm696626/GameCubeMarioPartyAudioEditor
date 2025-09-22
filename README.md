# GameCubeMarioPartyMusicEditor

### GameCube Mario Party Music Editor
* A tool allows you to edit the music in Mario Parties 4-7

### Why isn't the majority of Mario Party 4's soundtrack editable?
* Simple! Most of the music in that game is sequenced, not streamed

### Notes
* Music audio must be in DSP format and must have the end of the loop be the end of the song (which I believe most do, but check with vgmstream anyway)
  * https://katiefrogs.github.io/vgmstream-web/
* This is due to Mario Parties 4-7 not using end loop points
* Unfortunately, this tool cannot replace the last song in the file due to not knowing how (it's random garbage at where the file points for it)
  * Only real loss is not being to be able to replace Magma Flows for Party 6 (which is a Mic mode track anyway)
* I may have gotten some song names wrong due to duplicates or just me not knowing certain songs in these OSTs
  * This is prevalent especially with 5. They're probably alternate versions of minigame themes or jingles, but who knows?
* As of now, it's not possible to have songs that are larger than the original be the replacement (There's a size check in place)
  * Honestly, this is fine since Mario Party songs don't play for more than a minute anyway and the music restarts on a new event
* This tool will attempt to auto select the other track of your song, so name it either with the same file name appended with _L and _R or (channel 0) and (channel 1)
  * Example: mario_L.dsp and mario_R.dsp or luigi (channel 0).dsp and luigi (channel 1).dsp
  * It will do the same with the PDT itself too to identify what game it's for. Keep the file name untouched

### Song Dumping
* You can also dump the selected song to DSP format for listening in vgmstream or for conversion (it will dump in left and right channels)
  * The file size of the dumped file is deceiving for music replacement. The game doesn't care about that. It cares about the nibble count in the DSP header, which this tool already checks
  * Credit to Yoshimaster96's original dumping code which I translated into Java (I even fixed an issue where the stereo track didn't properly dump)
    * https://github.com/Yoshimaster96/mpgc-sound-tools

### Tip for Quality vs Size
* I recommend changing the sample rate of your track to either 32,000 (what Hudson used) or 24,000
* Keep your songs around the same length for higher quality (else you sacrifice sample rate for length)
* File size limitations are pretty strict and this tool doesn't rewrite the file's pointers
* I use this program to create DSPs
  * https://github.com/libertyernie/LoopingAudioConverter

### Special Thanks/Credits
* This project wouldn't be possible without the work of Yoshimaster96. Huge props to them!
* https://github.com/Yoshimaster96/mpgc-sound-tools
* Also, this documentation on the DSP format helped a lot too
* https://www.metroid2002.com/retromodding/wiki/DSP_(File_Format)