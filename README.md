# GameCubeMarioPartyMusicEditor

### GameCube Mario Party Music Editor
* A tool allows you to edit the music in Mario Parties 4-7

### Why isn't the majority of Mario Party 4's soundtrack editable?
* Simple! Most of the music in that game is sequenced, not streamed

### Notes
* Music audio must be in DSP format and must have the end of the loop be the end of the song (which I believe most do, but check with vgmstream anyway)
  * https://katiefrogs.github.io/vgmstream-web/
* This is due to Mario Parties 4-7 not using end loop points
* I may have gotten some song names wrong due to duplicates or just me not knowing certain songs in these OSTs
  * This is prevalent especially with 5. They're probably alternate versions of minigame themes or jingles, but who knows?
* As of now, it's not possible to have songs that are larger than the original be the replacement (There's a size check in place)
  * Honestly, this is fine since Mario Party songs don't play for more than a minute anyway and the music restarts on a new event
  * If anyone wants to figure this out, go right ahead!
* This tool will attempt to auto select the other track of your song, so name it either with the same file name appended with _L and _R or (channel 0) and (channel 1)
  * Example: mario_L.dsp and mario_R.dsp or luigi (channel 0).dsp and luigi (channel 1).dsp
  * It will do the same with the PDT itself as well in order to identify what game it's for. Keep the file name untouched

### Song Dumping
* You can also dump the selected song to DSP format for listening in vgmstream or for conversion (it will dump in left and right channels)
  * Credit to Yoshimaster96's original dumping code which I translated into Java
  * I fixed the following issues
    * The stereo track didn't properly dump (it dumped the same channel twice)
    * An end of file issue since the number of bytes was wrong for the audio data (should be nibbles/2 instead of nibbles << 1)
    * Changed the audio data to dump 8KB at a time instead of one byte for speed
  * Link to the code I used as a basis: https://github.com/Yoshimaster96/mpgc-sound-tools/blob/main/dump_pdt.c

### Tip for Quality vs Size
* I recommend changing the sample rate of your track (these are in Hz) (the ones listed here are 32kHz, 22.05 kHz, 16 kHz, and 8kHz)
  * 32000 (What Hudson Soft used) (make sure your song isn't longer)
  * 22050 (Half of CD quality)
  * 16000 (the bare minium to be listenable)
  * 8000 (compressed telephone quality)
* Keep your songs around the same length for higher quality (else you sacrifice sample rate for length)
* File size limitations are pretty strict and this tool doesn't rewrite the file's pointers
* I use this program to create DSPs
  * https://github.com/libertyernie/LoopingAudioConverter

### Documentation
* I've added a list of every song in the PDT by their internal order including unused DSPs
* Look in the documentation folder

### Special Thanks/Credits
* This project wouldn't be possible without the work of Yoshimaster96. Huge props to them!
* https://github.com/Yoshimaster96/mpgc-sound-tools
* Also, this documentation on the DSP format helped a lot too
* https://www.metroid2002.com/retromodding/wiki/DSP_(File_Format)