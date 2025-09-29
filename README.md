# GameCubeMarioPartyAudioEditor

### GameCube Mario Party Audio Editor
* A tool allows you to edit the music/sounds in Mario Parties 4-7

### Why isn't most of Mario Party 4's soundtrack editable?
* Simple! Most of the music in that game is sequenced, not streamed

### Music Notes
* Audio must be in DSP format split into two channels
* As of now, it's not possible to have songs that are larger in file size than the original be the replacement
  * Honestly, this is fine since Mario Party songs don't play for more than a minute anyway and the music restarts on a new event
  * It's rare if there'd be a case where a long song is needed anyway
* This tool will attempt to auto select the other track of your song, so name it either with the same file name appended with _L and _R or (channel 0) and (channel 1)
  * Example: mario_L.dsp and mario_R.dsp or luigi (channel 0).dsp and luigi (channel 1).dsp
  * It will do the same with the PDT itself as well in order to identify what game it's for. Keep the file name untouched

### Sound Notes
* Audio must be in DSP format (sounds are stored in mono DSPs within samp/sdir files so you need to rip them from their samp/sdirs first)
* As of now, it's not possible to have sounds that are larger in file size than the original be the replacement

### Song Dumping
* You can also dump the selected song to DSP format for listening in vgmstream or for conversion (it will dump in left and right channels)
    * Credit to Yoshimaster96's original dumping code which I translated into Java

### Sound Dumping
* You can also dump the selected sounds to their native samp/sdir format
    * Credit to Yoshimaster96's original dumping code which I translated into Java

### Sound Replacement
* You can also replace sound effects, but it's a bit more complicated
* Step 1: Convert your replacement sound to DSP (I use LoopingAudioConverter)
* Step 2: If your sound isn't meant to loop, use the Fix Nonlooping Sound DSP Header in the UI
* Step 3: If you don't have Python 3 installed, install it from https://www.python.org/downloads/
  * **Add Python to the path, or this won't work**
* Step 4: Download the Python script from https://github.com/Nisto/musyx-extract
* Step 5: Extract the sounds from the MSM with the UI (this will extract to samp/sdir)
* Step 6: Open command prompt and run the Python Script and follow the usage
* Step 7: Extract the sounds from the samp/sdir with the Python script
* Step 8: Replace whatever you want (make sure it's smaller)
* Step 9: Repack the files into their samp/sdir formats with the Python script
* Step 10: Use the UI to replace the appropriate sound bank
* Note: **Do not rename anything and make sure your replacements are the same name as the original**

### Tip for Quality vs Size
* I recommend lowering the sample rate of your track (the ones listed here are 22.05 kHz, 16 kHz, and 8kHz)
  * 22050 Hz (Half of CD quality)
  * 16000 Hz (the bare minium to be listenable)
  * 8000 Hz (compressed telephone quality)
* Keep your songs around the same length for higher quality
* Experiment to see what works best for the song you're using
* File size limitations are pretty strict and this tool doesn't rewrite the file's pointers
* I use this program to create DSPs
  * https://github.com/libertyernie/LoopingAudioConverter

### Special Thanks/Credits
* This project wouldn't be possible without the work of Yoshimaster96. Huge props to them! Their dumping code was crucial to get this project off the ground
  * https://github.com/Yoshimaster96/mpgc-sound-tools/blob/main/dump_pdt.c
* Also, this documentation on the DSP format helped a lot too
  * https://www.metroid2002.com/retromodding/wiki/DSP_(File_Format)
* Thanks to the Mario Wiki for pinpointing each song and catching a few I missed
  * https://www.mariowiki.com/Mario_Party_4_sound_test
  * https://www.mariowiki.com/Mario_Party_5_sound_test
  * https://www.mariowiki.com/Mario_Party_6_sound_test
  * https://www.mariowiki.com/Mario_Party_7_sound_test