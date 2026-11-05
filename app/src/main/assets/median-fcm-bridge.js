/**
 * M.I.C.D SCHOOL - MEDIAN.CO FIREBASE CLOUD MESSAGING JAVASCRIPT BRIDGE
 * Official integration for https://micd-smart-campus.base44.app
 * Connects web & mobile wrapper with Android FCM channels & device registration
 */

(function (window) {
  'use strict';

  const MICD_CHANNELS = {
    SCHOOL_ANNOUNCEMENTS: 'school_announcements',
    HOMEWORK_UPDATES: 'homework_updates',
    EXAMINATION_NOTICES: 'examination_notices',
    QUIZ_NOTIFICATIONS: 'quiz_notifications',
    COMPETITION_NOTIFICATIONS: 'competition_notifications'
  };

  const MicdMedianFCM = {
    isBridgeReady: false,
    currentToken: null,
    subscribedTopics: new Set(),
    listeners: {
      onTokenReceived: null,
      onNotificationReceived: null,
      onNotificationClicked: null,
      onPermissionStatus: null
    },

    /**
     * 1. Initialize and detect Median.co JavaScript Bridge
     */
    init: function (options) {
      options = options || {};
      const self = this;

      if (options.onTokenReceived) this.listeners.onTokenReceived = options.onTokenReceived;
      if (options.onNotificationReceived) this.listeners.onNotificationReceived = options.onNotificationReceived;
      if (options.onNotificationClicked) this.listeners.onNotificationClicked = options.onNotificationClicked;
      if (options.onPermissionStatus) this.listeners.onPermissionStatus = options.onPermissionStatus;

      // Check if window.median is injected
      if (window.median && window.median.firebaseMessaging) {
        self.isBridgeReady = true;
        self._setupListeners();
        console.log('[MICD FCM] Median.co Firebase Bridge detected and ready.');
      } else {
        // Wait for median bridge ready event
        window.addEventListener('median_ready', function () {
          self.isBridgeReady = true;
          self._setupListeners();
          console.log('[MICD FCM] Median.co bridge ready event received.');
        });

        // Fallback polling for asynchronous bridge injection
        let attempts = 0;
        const interval = setInterval(function () {
          attempts++;
          if (window.median && window.median.firebaseMessaging) {
            self.isBridgeReady = true;
            clearInterval(interval);
            self._setupListeners();
            console.log('[MICD FCM] Median bridge detected after polling.');
          } else if (attempts > 20) {
            clearInterval(interval);
            console.warn('[MICD FCM] Median.co Bridge not detected. Running in standalone browser/app.');
          }
        }, 250);
      }
    },

    /**
     * Check if currently executing inside Median app wrapper
     */
    isMedianWrapper: function () {
      return !!(window.median || navigator.userAgent.includes('median') || navigator.userAgent.includes('gonative'));
    },

    /**
     * 2. Request native Android notification permission
     * Uses median.firebaseMessaging.requestPermission()
     */
    requestPermission: function (callback) {
      const self = this;
      if (window.median && window.median.firebaseMessaging && typeof window.median.firebaseMessaging.requestPermission === 'function') {
        window.median.firebaseMessaging.requestPermission().then(function (result) {
          const granted = !!(result && (result.enabled || result.granted || result.status === 'granted'));
          if (callback) callback(granted);
          if (self.listeners.onPermissionStatus) self.listeners.onPermissionStatus(granted);
        }).catch(function (err) {
          console.error('[MICD FCM] Error requesting permission:', err);
          if (callback) callback(false, err);
        });
      } else {
        console.warn('[MICD FCM] Native notifications require the Median Android app.');
        if (callback) callback(false, 'Median Bridge unavailable');
      }
    },

    /**
     * 3. Check current permission status
     * Uses median.firebaseMessaging.checkPermission()
     */
    checkPermission: function (callback) {
      if (window.median && window.median.firebaseMessaging && typeof window.median.firebaseMessaging.checkPermission === 'function') {
        window.median.firebaseMessaging.checkPermission().then(function (result) {
          const granted = !!(result && (result.enabled || result.granted || result.status === 'granted'));
          if (callback) callback(granted);
        }).catch(function (err) {
          if (callback) callback(false, err);
        });
      } else {
        if (callback) callback(false, 'Median Bridge unavailable');
      }
    },

    /**
     * 4. Register with Firebase and retrieve device token
     * Uses median.firebaseMessaging.register() and getToken()
     */
    registerDevice: function (userId, userRole, classNumber, callback) {
      const self = this;
      if (!self.isMedianWrapper()) {
        const errorMsg = 'Native push notifications require the Median.co Android application wrapper.';
        console.warn('[MICD FCM] ' + errorMsg);
        if (callback) callback(null, errorMsg);
        return;
      }

      if (window.median && window.median.firebaseMessaging) {
        // Register device
        window.median.firebaseMessaging.register().then(function (regResponse) {
          // Retrieve FCM registration token
          return window.median.firebaseMessaging.getToken();
        }).then(function (tokenResponse) {
          const token = (tokenResponse && tokenResponse.token) ? tokenResponse.token : tokenResponse;
          self.currentToken = token;
          console.log('[MICD FCM] FCM Registration Token obtained:', token);

          // Auto-subscribe to relevant school topics
          self.subscribeToTopic('school_all');
          if (userRole === 'STUDENT' && classNumber) {
            self.subscribeToTopic('class_' + classNumber);
          } else if (userRole === 'TEACHER') {
            self.subscribeToTopic('school_teachers');
          } else if (userRole === 'PARENT') {
            self.subscribeToTopic('school_parents');
          }

          if (callback) callback(token);
          if (self.listeners.onTokenReceived) self.listeners.onTokenReceived(token);
        }).catch(function (err) {
          console.error('[MICD FCM] Failed to register FCM token:', err);
          if (callback) callback(null, err);
        });
      }
    },

    /**
     * 5. Subscribe to FCM topic
     */
    subscribeToTopic: function (topic) {
      if (window.median && window.median.firebaseMessaging && typeof window.median.firebaseMessaging.subscribeToTopic === 'function') {
        window.median.firebaseMessaging.subscribeToTopic({ topic: topic });
        this.subscribedTopics.add(topic);
        console.log('[MICD FCM] Subscribed to topic:', topic);
      }
    },

    /**
     * 6. Unsubscribe from FCM topic
     */
    unsubscribeFromTopic: function (topic) {
      if (window.median && window.median.firebaseMessaging && typeof window.median.firebaseMessaging.unsubscribeFromTopic === 'function') {
        window.median.firebaseMessaging.unsubscribeFromTopic({ topic: topic });
        this.subscribedTopics.delete(topic);
        console.log('[MICD FCM] Unsubscribed from topic:', topic);
      }
    },

    /**
     * 7. Setup incoming and click listeners
     */
    _setupListeners: function () {
      const self = this;

      // Handle foreground notifications received
      if (window.median && window.median.onNotificationReceived) {
        window.median.onNotificationReceived = function (data) {
          console.log('[MICD FCM] Foreground Notification Received:', data);
          if (self.listeners.onNotificationReceived) {
            self.listeners.onNotificationReceived(data);
          }
        };
      }

      // Handle notification click action & deep-linking
      if (window.median && window.median.onNotificationClicked) {
        window.median.onNotificationClicked = function (data) {
          console.log('[MICD FCM] Notification Clicked:', data);
          if (data && data.url) {
            window.location.href = data.url;
          }
          if (self.listeners.onNotificationClicked) {
            self.listeners.onNotificationClicked(data);
          }
        };
      }
    }
  };

  window.MicdMedianFCM = MicdMedianFCM;
  window.MICD_CHANNELS = MICD_CHANNELS;
})(window);
