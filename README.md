# PhotoGallery

Photo Gallery is a app which looks for images in Flickr and downloads and represents them to the user, also a search bar is present
to search for photos.

The idea behind this app is to implement Async task for downloading the images from flicker with a HTTP GET request,
which returns us JSON, which we then parse to get the particular image. The app also uses Piccaso and GSON libraries, and teaches
their use. 
Using Job Service(or Alarm in older version of android) the app is responsipble for Polling Flicker for new images,
and presenting them to the user with a notification which if clicked opens the app itself.
Here I Learned also how to use Broadcast Receivers to listen if the device after turned of has been turned on, which
fires my service to tell the app to start polling again for new images and display notifications to the user.
The last function in this app is when an image is pressed, the app redirects the user to a webview which displays
the image in flickr's website.
