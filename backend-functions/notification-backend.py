from datetime import datetime, timedelta, timezone
import firebase_admin
from firebase_admin import firestore, messaging
from firebase_functions import scheduler_fn

firebase_admin.initialize_app()

@scheduler_fn.on_schedule(schedule="every day 00:00")
def send_inactivity_notifications(event):
    db = firestore.client()
    users_ref = db.collection("users")
    users = users_ref.stream()

    now = datetime.now(timezone.utc)
    notifications_sent = 0

    for user_doc in users:
        data = user_doc.to_dict()

        fcm_token = data.get("fcmToken")
        last_active = data.get("lastActive")
        notif_pref = data.get("notificationPreference", "Off")  # All / Minimal / Off

        if not fcm_token or not last_active:
            continue

        if now - last_active < timedelta(hours=24):
            continue
        if notif_pref != "All":
            continue

        message = messaging.Message(
            notification=messaging.Notification(
                title="You hungry?",
                body="Open FitBite to log your meals for the day!"
            ),
            token=fcm_token
        )

        try:
            messaging.send(message)
            notifications_sent += 1
        except Exception as e:
            print(f"Failed to send to {user_doc.id}: {e}")

    print(f"Notifications sent: {notifications_sent}")
