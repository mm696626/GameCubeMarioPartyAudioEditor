# GameCubeMarioPartyMusicEditor

### GameCube Mario Party Music Editor
* A tool allows you to edit the music in Mario Parties 5-7

### Why isn't Mario Party 4's soundtrack editable?
* Simple! Most of the music in that game is sequenced, not streamed
* There's only a few songs that are even in the PDT, so I didn't bother

### Notes
* Music audio must be in DSP format
* As of now, it's not possible to have songs that are larger than the original be the replacement
  * Honestly, this is fine since Mario Party songs don't play for more than a minute anyway and the music restarts on a new event
  * If anyone wants to figure this out, go right ahead!
* This tool will attempt to auto select the other track of your song, so name it either with the same file name appended with _L and _R or (channel 0) and (channel 1)
  * Example: mario_L.dsp and mario_R.dsp or luigi (channel 0).dsp and luigi (channel 1).dsp
  * It will do the same with the PDT itself as well in order to identify what game it's for. Keep the file name untouched

### Tip for Quality vs Size
* I recommend changing the sample rate of your track (the ones listed here are 32kHz, 22.05 kHz, 16 kHz, and 8kHz)
  * 32000 Hz (What Hudson Soft used) (make sure your song isn't longer)
  * 22050 Hz (Half of CD quality)
  * 16000 Hz (the bare minium to be listenable)
  * 8000 Hz (compressed telephone quality)
* Keep your songs around the same length for higher quality
* File size limitations are pretty strict and this tool doesn't rewrite the file's pointers
* I use this program to create DSPs
  * https://github.com/libertyernie/LoopingAudioConverter

### Special Thanks/Credits
* This project wouldn't be possible without the work of Yoshimaster96. Huge props to them!
  * https://github.com/Yoshimaster96/mpgc-sound-tools/blob/main/dump_pdt.c
* Also, this documentation on the DSP format helped a lot too
  * https://www.metroid2002.com/retromodding/wiki/DSP_(File_Format)
* Thanks to the Mario Wiki for pinpointing each song and catching a few I missed
  * https://www.mariowiki.com/Mario_Party_5_sound_test
  * https://www.mariowiki.com/Mario_Party_6_sound_test
  * https://www.mariowiki.com/Mario_Party_7_sound_test