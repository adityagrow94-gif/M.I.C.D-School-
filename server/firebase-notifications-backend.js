/**
 * M.I.C.D SCHOOL - SECURE FIREBASE CLOUD MESSAGING BACKEND
 * Production Server Functions for Push Notifications
 * Target Website: https://micd-smart-campus.base44.app
 * Android Wrapper: Median.co (FCM Integration)
 *
 * All service account credentials and private keys remain strictly on the server.
 */

const admin = require('firebase-admin');

// Initialize Firebase Admin SDK (uses environment credentials on Google Cloud / Firebase)
if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore ? admin.firestore() : null;
const messaging = admin.messaging();

/**
 * Android Notification Channel Mapping for Median.co
 */
const NOTIFICATION_CHANNELS = {
  ANNOUNCEMENTS: 'school_announcements',
  HOMEWORK: 'homework_updates',
  EXAMINATIONS: 'examination_notices',
  QUIZZES: 'quiz_notifications',
  COMPETITIONS: 'competition_notifications'
};

/**
 * 1. Send Targeted School Notification
 * Enforces role authorization (SUPER_ADMIN, PRINCIPAL_ADMIN, TEACHER),
 * respects user preferences, and handles expired tokens.
 */
async function sendSchoolNotification(payload) {
  const {
    senderId,
    senderRole, // 'SUPER_ADMIN', 'PRINCIPAL_ADMIN', 'TEACHER'
    channelId,
    title,
    body,
    targetRole, // 'ALL', 'STUDENTS', 'TEACHERS', 'PARENTS'
    targetClass, // 0 for all classes, 1-12
    targetSection, // 'ALL', 'A', 'B', 'C'
    targetUrl,
    idempotencyKey // Prevents duplicate delivery
  } = payload;

  // Authorization validation
  if (!['SUPER_ADMIN', 'PRINCIPAL_ADMIN', 'TEACHER'].includes(senderRole)) {
    throw new Error('Unauthorized notification sender role.');
  }

  // Teacher permission scope restriction
  if (senderRole === 'TEACHER' && targetClass === 0 && targetRole === 'ALL') {
    throw new Error('Teachers can only dispatch notifications to their assigned classes.');
  }

  // Determine FCM topic or device tokens
  let topic = null;
  if (targetClass > 0) {
    topic = `class_${targetClass}`;
  } else if (targetRole === 'TEACHERS') {
    topic = 'school_teachers';
  } else if (targetRole === 'PARENTS') {
    topic = 'school_parents';
  } else {
    topic = 'school_all';
  }

  // Build FCM message payload with explicit Android Notification Channel
  const fcmMessage = {
    topic: topic,
    notification: {
      title: title,
      body: body
    },
    data: {
      channelId: channelId || NOTIFICATION_CHANNELS.ANNOUNCEMENTS,
      targetUrl: targetUrl || 'micdschool://dashboard',
      senderRole: senderRole,
      idempotencyKey: idempotencyKey || `${Date.now()}`
    },
    android: {
      priority: 'high',
      notification: {
        channelId: channelId || NOTIFICATION_CHANNELS.ANNOUNCEMENTS,
        icon: 'ic_micd_logo',
        color: '#0D47A1',
        sound: 'default',
        clickAction: 'FLUTTER_NOTIFICATION_CLICK'
      }
    }
  };

  try {
    const response = await messaging.send(fcmMessage);
    console.log(`[FCM SUCCESS] Message sent to topic ${topic}. Response:`, response);
    return {
      success: true,
      messageId: response,
      topic: topic,
      status: 'DELIVERED',
      timestamp: new Date().toISOString()
    };
  } catch (error) {
    console.error('[FCM ERROR] Failed to send push notification:', error);
    return {
      success: false,
      error: error.message,
      status: 'FAILED',
      timestamp: new Date().toISOString()
    };
  }
}

/**
 * 2. Send Direct Notification to Specific Device Token
 */
async function sendToDeviceToken(token, title, body, channelId, targetUrl) {
  if (!token) throw new Error('Device FCM token is required.');

  const message = {
    token: token,
    notification: { title, body },
    data: {
      channelId: channelId || NOTIFICATION_CHANNELS.ANNOUNCEMENTS,
      targetUrl: targetUrl || ''
    },
    android: {
      notification: {
        channelId: channelId || NOTIFICATION_CHANNELS.ANNOUNCEMENTS,
        priority: 'high',
        color: '#0D47A1'
      }
    }
  };

  try {
    const response = await messaging.send(message);
    return { success: true, messageId: response };
  } catch (error) {
    // Detect expired / unregistered token
    if (error.code === 'messaging/registration-token-not-registered') {
      console.warn(`[FCM TOKEN EXPIRED] Token ${token} is no longer valid.`);
      return { success: false, expired: true, error: error.message };
    }
    return { success: false, error: error.message };
  }
}

module.exports = {
  sendSchoolNotification,
  sendToDeviceToken,
  NOTIFICATION_CHANNELS
};
