const fs = require("fs");

const fileIn = process.argv[2];
const fileOut = process.argv[3];

console.log(fileIn);

let out = "";

fs.readFile(fileIn, "utf-8", (e,d)=>{
  if(e){
    return console.error("error" + e);
  }
  console.log("in data");

  d.split("\n").forEach(l=>{
    let parts = l.split(",");
    out += parts[0] + "," + parts[1] + "," + parts[2] + "," + parts[4] + "\n";
  });

  fs.writeFile(fileOut, out, function(err) {
    if(err) {
      return console.log(err);
    }

    console.log("Saved result to " + fileOut);
  });



});

