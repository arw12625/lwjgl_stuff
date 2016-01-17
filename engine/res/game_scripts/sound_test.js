


var monoRes = resourceManager.loadResource("sound/monoTest.ogg", new sound.SoundData());

var monoName = "mono";
var monoSourceName = "monoTest";
soundManager.loadBuffer(monoName, monoRes.getData());
soundManager.createSource(monoSourceName);
soundManager.setSourcePosition(monoSourceName, -10,0,0);
soundManager.setSourceBuffer(monoSourceName, monoName, true, true);


var jazzRes = resourceManager.loadResource("sound/stereoTest.ogg", new sound.SoundData());

soundManager.setListenerPosition(0,0,0);
var bufferName = "jazz";
var sourceName = "background";
soundManager.loadBuffer(bufferName, jazzRes.getData());
soundManager.createSource(sourceName);
soundManager.setSourceGain(sourceName, 0);
//soundManager.setSourceBuffer(sourceName, bufferName, true, true);