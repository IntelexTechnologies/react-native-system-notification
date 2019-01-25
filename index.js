/**
* React Native Progress Notification
*/

import { NativeModules } from 'react-native';

const { NotificationModule } = NativeModules;

const RNPN = {
  create(attrs = {}) {
    return new Promise((resolve, reject) => {
      NotificationModule.rnGetAppName((e) => {}, (appName) => {
        // attrs.subject = appName;
        attrs.appName = appName

        attrs = encNativeNotification(attrs);

        NotificationModule.rnSend(attrs.id, attrs, reject, (notification) => {
          resolve(decNativeNotification(notification));
        });
      });
    });
  },
  delete(id) {
    return new Promise((resolve, reject) => {
      NotificationModule.rnDelete(id, reject, (notification) => {
        resolve(decNativeNotification(notification));
      });
    });
  }
}

const methodsDecEnc = {
  payload: (val, func) => {
    if (func === 'dec') {
      if (!val) {
        return {};
      } else {
        return JSON.parse(val);
      }
    } else {
      if (!val) {
        val = {};
      }

      return JSON.stringify(val);
    }
  },
  smallIcon: val => !val ? 'ic_launcher' : val,
  id: val => !val ? parseInt(Math.random() * 100000) : val,
  action: val => !val ? 'DEFAULT' : val,
  autoClear: val => !val ? true : val,
  priority: val => val === undefined ? 1 : val,
  sound: val => val === undefined ? 'default' : val,
  vibrate: val => val === undefined ? 'default' : val,
  lights: val => val === undefined ? 'default' : val,
  isOngoing: val => val === true,
  showAppName: val => val === true,
};

function normilizeAttrsLoop(attrs, func = 'enc') {
  for (let key in attrs) {
    if (methodsDecEnc[key]) {
      attrs[key] = methodsDecEnc[key](attrs[key], func);
    }
  }

  if (func === 'enc') {
    // Set default values
    for (let key in methodsDecEnc) {
      attrs[key] = methodsDecEnc[key](attrs[key], func);
    }
  }

  return attrs;
}

function decNativeNotification(attrs) {
  attrs = normilizeAttrsLoop(attrs, 'dec');

  if (attrs.progress) {
    attrs.progress = attrs.progress / 1000;
  }

  return attrs;
}

function encNativeNotification(attrs) {
  if (typeof attrs === 'string') {
    attrs = JSON.parse(attrs);
  }

  attrs = normilizeAttrsLoop(attrs, 'enc');

  if (attrs.tickerText === undefined) {
    if (attrs.subject) {
      attrs.tickerText = attrs.subject + ': ' + attrs.message;
    } else {
      attrs.tickerText = attrs.message;
    }
  }

  attrs.delayed = (attrs.delay !== undefined);
  attrs.scheduled = (attrs.schedule !== undefined);

  if (attrs.progress) {
    attrs.progress = attrs.progress * 1000;
  }

  return attrs;
}

module.exports = RNPN;
