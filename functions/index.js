/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */
const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendNotificationOnNewQueja = functions.firestore
    .document("complaints/{complaintId}")
    .onCreate((snap, context) => {
      const newComplaint = snap.data();
      const asunto = newComplaint.asunto;
      const userId = newComplaint.userId;

      return admin.firestore().collection("users").doc(userId).get()
          .then((userDoc) => {
            if (!userDoc.exists) {
              throw new Error("Usuario no encontrado");
            }
            const userName = userDoc.data().userName;

            return admin.firestore().collection("users")
                .where("userType", "==", "admin")
                .get()
                .then((snapshot) => {
                  const tokens = [];
                  snapshot.forEach((doc) => {
                    tokens.push(doc.data().fcmToken);
                  });

                  // Crea el mensaje de la notificación
                  const message = {
                    notification: {
                      title: "Nueva Queja",
                      body: `${userName} ha realizado una queja.
                      Asunto: ${asunto}`,
                    },
                    tokens: tokens,
                  };

                  // Envía la notificación a través de FCM
                  return admin.messaging().sendMulticast(message);
                })
                .then((response) => {
                  // Manejar la respuesta del envío de la notificación
                  console.log("Notificaciones enviadas:", response);
                })
                .catch((error) => {
                  // Manejar errores
                  console.error("Error al enviar notificaciones:", error);
                });
          })
          .catch((error) => {
            console.error("Error al obtener el usuario:", error);
          });
    });

exports.sendNotificationOnNewComment = functions.firestore
    .document("complaints/{complaintId}/comments/{commentId}")
    .onCreate((snap, context) => {
      const newComment = snap.data();
      const complaintId = context.params.complaintId;

      return admin.firestore().collection("complaints").doc(complaintId)
          .get()
          .then((doc) => {
            if (!doc.exists) {
              throw new Error("Queja no encontrada");
            }
            const complaint = doc.data();
            const userId = complaint.userId;

            return admin.firestore().collection("users").doc(userId).get();
          })
          .then((userDoc) => {
            if (!userDoc.exists) {
              throw new Error("Usuario no encontrado");
            }
            const token = userDoc.data().fcmToken;
            if (token) {
              const payload = {
                notification: {
                  title: "Nuevo Comentario en tu Queja",
                  body: newComment.comment,
                },
              };
              return admin.messaging().sendToDevice(token, payload);
            } else {
              console.log("No se encontró el token para:", userDoc.id);
              return null;
            }
          })
          .then((response) => {
            if (response) {
              console.log("Mensaje enviado exitosamente:", response);
            }
            return null;
          })
          .catch((error) => {
            console.error("Error al enviar notificación:", error);
          });
    });
