"use strict"

const electron = require('electron');
const path = require('path');
const url = require('url');
const mscsvparse = require('./scripts/mainsequence-parser');
const windowStateKeeper = require('electron-window-state');

// SET ENV
process.env.NODE_ENV = 'development';

const { app, BrowserWindow, Menu, ipcMain, dialog } = electron;


let mainWindow;

// Listen for app to be ready
app.on('ready', function () {
    let mainWindowState = windowStateKeeper({
        defaultWidth: 800,
        defaultHeight: 800
    });

    mainWindow = new BrowserWindow({
        'x': mainWindowState.x,
        'y': mainWindowState.y,
        'width': mainWindowState.width,
        'height': mainWindowState.height
    });

    mainWindow.loadURL(url.format({
        pathname: path.join(__dirname, 'html', 'mainSequence.html'),
        protocol: 'file:',
        slashes: true
    }));

    mainWindow.on('closed', function () {
        app.quit();
    });

    mainWindow.once('ready-to-show', () => {
        win.show()
    });
    
    mainWindowState.manage(mainWindow);

    const mainMenu = Menu.buildFromTemplate(mainMenuTemplate);
    Menu.setApplicationMenu(mainMenu);
});


// Create menu template
const mainMenuTemplate = [
    {
        label: 'File',
        submenu: [
            {
                label: 'Open',
                accelerator: 'Ctrl+O',
                click() {
                    openFile((data) => {fileLoaded(data)});
                }
            },
            {
                label: 'Quit',
                accelerator: process.platform == 'darwin' ? 'Command+Q' : 'Ctrl+Q',
                click() {
                    app.quit();
                }
            }
        ]
    }
];

function openFile(callback)
{
    dialog.showOpenDialog({ 
            filters: [
                { name: 'comma separated values', extensions: ['csv'] }
            ],
            multiSelections: false
        },
        (files) => {
            const results = mscsvparse.parse(files[0], (data) => {
                callback(data);
            });
        }
    );
}

function fileLoaded(data)
{
    console.log('loaded = ' + data);
    // parse into objects
    // populate chart
    mainWindow.webContents.send('data:loaded', data);
}

// Add developer tools option if in dev
if (process.env.NODE_ENV !== 'production') {
    mainMenuTemplate.push({
        label: 'Developer Tools',
        submenu: [
            {
                role: 'reload'
            },
            {
                label: 'Toggle DevTools',
                accelerator: process.platform == 'darwin' ? 'Command+I' : 'Ctrl+I',
                click(item, focusedWindow) {
                    focusedWindow.toggleDevTools();
                }
            }
        ]
    });
}