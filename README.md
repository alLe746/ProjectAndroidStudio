# ProjectAndroidStudio
Project for Mobile Dev by Alexis Ledoux

# HOW TO USE
The first time you use the app, you are asked to register.
just put the ID of one of the client (from 1 to 7)

If you already used this app (or you have some rest of this apk), you get a connexion page.
You have to put the same ID that you have already set when you registered.
If you don't remember it, you can reset. All local database will be lost, and you will be redirected to the register page

WHen you registered/logged in, you can access the values. By default, you get the local results (so you get nothing if you just registered or never updated)
When you want to update the database, you simply click on the update button. The connexion will be done in background, with an error message if the connexion didn't work (a toast).


#SECURE SAVING OF DATA
The program is using 2 tables in a app.db3 database in sqlite3.
The implementation of sqlite3 in android makes retrieving data not easy, with protections such as rstriction of access for others applications
To identify yourself, you ID is compared to a hashed version of your ID, mixed with a salt. The algorithm is SHA-512. So the database dosn't have your ID in clear text

For the local data abount accounts, i choose to encode it using a base64 encoding method.

#hiding API URL

to somewhat hide the APi url, i'm not using it as a celar text string in my source code, but i'm referring to a string i declared in my gradle.properties file. It can still be retrieved, but it's way harder (+ the API is not secured anyway)

![image](https://user-images.githubusercontent.com/52492246/110243767-13276600-7f5c-11eb-86f7-e201cdd46dbb.png)
Image of the register View

![image](https://user-images.githubusercontent.com/52492246/110243784-2a665380-7f5c-11eb-93c1-43aca4efd89a.png)
image of the account View, the first time you logged in ("Liste vide")

![image](https://user-images.githubusercontent.com/52492246/110243811-48cc4f00-7f5c-11eb-8c52-8bc0a722d377.png)
image of the same view after clicking the update button

![image](https://user-images.githubusercontent.com/52492246/110243828-5bdf1f00-7f5c-11eb-96e2-417288356369.png)
image of the start of the application (connecting view), if you already use the application before
The reset button sends you back to the register page



