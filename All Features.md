# MedTrack: All Features

MedTrack is a family medication management app that helps families stay informed when important medications are missed.

## 🔐 Authentication

- Create an account with email and password.
- Email verification for new accounts.
- Secure login and logout.
- Password reset through email.
- Clear authentication error messages.

## 👤 Profile

- Create and edit a personal profile.
- Choose gender and receive a matching avatar.
- Set age and optional About Me information.
- Manage connected Family Hubs from the profile.

## 👨‍👩‍👧‍👦 Family Hub

Create or join a shared family medication space.

### Create Hub
- Create a hub with a custom name.
- Generate a unique 6-character Hub Code.
- Share the code with family members.

### Join Hub
- Enter a Hub Code to request access.
- Wait for the Hub Creator to approve the request.
- Pending requests remain active even if the app is closed or refreshed.

## 💊 Medication Management

- Add medications with name, dosage, recipient, photo, notes, reminder time, and cycle.
- Choose an approved adult or child as the recipient.
- Edit or delete medications created by you.
- View medications shared within the hub.

## 🔄 Reminder Cycles

- Every 24 hours
- Every 48 hours
- Custom intervals

Each cycle creates its own medication occurrence. Once the cycle resets, the medication returns to **Mark as taken** for the new occurrence.

## ⏰ Smart Reminders

MedTrack uses staged reminders when a medication is not marked as taken:

1. Scheduled reminder to the assigned person.
2. Missed-dose reminder to the same person.
3. Family notification if the dose remains unmarked.

Family escalation is calculated from the original medication reminder time.

## ✅ Taken Tracking

- Adult medications can only be marked as taken by the assigned person.
- Any approved hub member can mark a child's medication as taken.
- The exact time the dose was marked taken is recorded.
- Marking a dose as taken stops further reminders for that occurrence.

## 👶 Child Profiles

- Create dependent child profiles without requiring an account.
- Choose a child avatar and gender.
- Assign an approved adult responsible for medication reminders.
- Child medications can be managed by the family.

## 🎙️ Smart Voice Memo

Speak medication information naturally instead of entering every field manually.

MedTrack can extract:

- Medicine
- Dosage
- Time
- Recipient
- Reminder cycle
- Notes

The information is placed into the normal medication form for review before creation.

## 📋 Medication History

View a dated record of meaningful activity inside the hub, including:

- Medication changes
- Doses marked as taken
- Members joining or leaving
- Child profile changes
- Hub management actions

Activity can be browsed by date.

## 👥 Hub Management

Creators can:

- Approve or reject join requests.
- Manage hub members.
- Create and manage child profiles.
- Transfer Creator ownership.
- Delete the Family Hub.

Members can leave a hub when permitted.

## 🔔 Hub Reminder Settings

Each Family Hub can configure:

- Time before a missed-dose reminder.
- Time before notifying family members.

These settings are specific to each hub.

## 📱 Android Notifications

Medication reminders work through Android's notification system, including:

- Background notifications
- Lock-screen notifications where permitted
- Notification actions
- Notification permissions
- Persistent reminder scheduling

## 🔄 Real-Time Updates

Important hub changes update across connected devices, including:

- Join approvals
- Medication status
- Member changes
- Child profile changes
- Hub deletion
- Creator transfers

## 🎨 Design

- Sora typography throughout the app.
- Warm family-oriented color palette.
- Responsive Android interface.
- Compact inline errors and feedback.
- Smooth, restrained animations.
