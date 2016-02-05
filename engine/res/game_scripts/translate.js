var trans = obj.getChild("Transform");
var v = new joml.Vector3f((Math.random() - 0.5) / 20, (Math.random() - 0.5) / 20, (Math.random() - 0.5) / 10);
var t = new joml.Quaternionf(new joml.AxisAngle4f(Math.random() / 10, Math.random(), Math.random(), Math.random()).normalize());
t.normalize();
var time = 0;
addUpdate(function (delta) {
    //print(delta);
    time+= delta;
    if(time < 3000) {
        
    trans.translate(v);
    trans.rotate(t);
    
    }
});
addDispatch("removed", function() {print("oh noes");});