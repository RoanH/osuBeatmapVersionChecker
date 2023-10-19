# osu! Map Version Checker
Simple program to check for new versions of unranked beatmaps.

**Note: This project is no longer actively maintained due to low interest and an osu! update breaking the program.**

# Forum post
Link: https://osu.ppy.sh/forum/p/6222182#p6222182

## Beatmap Version Checker

I wanted to make something that would make updating local pending/unranked beatmaps easier. The result is the following program. It'll scan your songs collection for unranked maps and check whether or not there is an update available for them or if their ranked status changed. After that you can either have the maps be updated automatically or update them youself.

To use the program you will need an osu! API key, which you can get [here](https://osu.ppy.sh/p/api]).

The program itself looks like this:<br>
![img](http://i.imgur.com/PQW4ktR.png)
![img](http://i.imgur.com/KZat5ZD.png)
![img](http://i.imgur.com/bzanBJD.png)

That's it, I hope some of you will find this program useful  :) 

## Notes
- The automatic updating system isn't perfect, you'll still have to navigate to it ingame to have osu! register it as updated.
- For some reason sometimes the automatic updating doesn't work.
- There's an explanation about the "API poll rate" under the "help" button.
- The estimate times shown are under ideal circumstances (0 ms network latency) in my experience these estimates are incredibly inaccurate  :P 

## Downloads
(Java 8 required)<br>
[Windows executable](https://github.com/RoanH/osuBeatmapVersionChecker/releases/download/v1.1/osuBeatmapVersionChecker-v1.1.exe)<br>
[Runnable Java Archive](https://github.com/RoanH/osuBeatmapVersionChecker/releases/download/v1.1/osuBeatmapVersionChecker-v1.1.jar)

All releases: [link](https://github.com/RoanH/osuBeatmapVersionChecker/releases)<br>
GitHub repository: [osuBeatmapVersionChecker](https://github.com/RoanH/osuBeatmapVersionChecker)

## History
Project development started: 19th of June, 2017.
