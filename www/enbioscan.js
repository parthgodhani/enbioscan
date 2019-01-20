var exec = require('cordova/exec');

module.exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'enbioscan', 'coolMethod', [arg0]);
};

module.exports.init = function(arg0,success,error){
    exec(success, error,'enbioscan', 'init', [arg0])
}
