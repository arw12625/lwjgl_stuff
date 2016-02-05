
return {
    
    cls : function() {obj.clearConsole(); },
    
    echo : function(msg) {obj.println(msg); },
    
    evaluateLine:function(line) { return eval(line); }
}

