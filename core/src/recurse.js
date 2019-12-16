const fs = require("fs");
function list(path){
    if(fs.statSync(path).isDirectory()){
        fs.readdirSync(path).forEach(entry => list(path + "/" + entry));
    } else {
        const log = path.slice(0, path.length - 4);
        console.log(log.replace(/\//g, ".") + "class,");
    }
}
list("io");