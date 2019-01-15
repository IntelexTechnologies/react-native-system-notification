
# react-native-progress-notification

## Getting started

`$ npm install react-native-progress-notification --save`

### Manual installation

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.intelex.reactnative.notification.NotificationPackage;` to the imports at the top of the file
  - Add `new NotificationPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-progress-notification'
  	project(':react-native-progress-notification').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-progress-notification/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-progress-notification')
  	```


## Usage
```javascript
import NotificationAndroid from 'react-native-progress-notification';

NotificationAndroid.create({
  id: 1,
  subject: 'Your title',
  message: 'Your message',
  smallIcon: 'ic_launcher',
  progress: 0.2,
  onlyAlertOnce: true,
});

```
