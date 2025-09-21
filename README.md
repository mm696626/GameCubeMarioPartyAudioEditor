# GameCubeMarioPartyMusicEditor

### GameCube Mario Party Music Editor
* A tool allows you to edit the music in Mario Parties 5-7

### Why not Mario Party 4?
* Simple! Most of the music in that game is sequenced, not streamed

### Note
* Music audio must be in DSP format and must have the end of the loop be the end of the song
* This is due to Mario Parties 5-7 not using end loop points
* Unfortunately, this tool cannot replace the last song in the file due to not knowing how (it's random garbage at where the file points for it)
* I may have gotten some song names wrong due to duplicates or just me not knowing certain songs in these OSTs
* As of now, it's not possible to have songs that are larger in file size be the replacement (There's a size check in place)

### Special Thanks
* This project wouldn't be possible without the work of Yoshimaster96. Huge props to them!
* https://github.com/Yoshimaster96/mpgc-sound-tools
* Also, this documentation on the DSP format helped a lot too
* https://www.metroid2002.com/retromodding/wiki/DSP_(File_Format)