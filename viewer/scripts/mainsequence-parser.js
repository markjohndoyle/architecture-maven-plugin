"use strict"

const fs = require('fs');
const csvparse = require('csv-parse');
var transform = require('stream-transform');

const parser = csvparse({delimiter: ','})

function parse(filePath, callback)
{
    fs.readFile(filePath, 'utf8', (err, data) => 
    {
        csvparse(data, (err, output) => {
            callback(output);
        });
    });
}

function createModule(record)
{
    console.log("creating module from " + record);
    return { moduleName:record[0], 
             linstability:record[1], 
             abstraction:record[2], 
             distance:record[3]
            };
}

module.exports = {
    parse
}