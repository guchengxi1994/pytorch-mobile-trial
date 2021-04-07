import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';

class MainPage extends StatefulWidget {
  @override
  MainPageState createState() => MainPageState();
}

class MainPageState extends State {
  var _imgPath;
  String filename;
  final platform = MethodChannel("face.convert");
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter call Android method',
      debugShowCheckedModeBanner: false,
      home: Scaffold(
          appBar: AppBar(
            title: Text('Flutter call Android method'),
          ),
          body: SingleChildScrollView(
            child: Column(
              children: [
                _ImageView(_imgPath),
                Container(
                    child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    ElevatedButton(
                      onPressed: () async {
                        print("babalaka");
                        var image = await ImagePicker.pickImage(
                            source: ImageSource.gallery);
                        setState(() {
                          _imgPath = image;
                          print(image.path);
                          filename = image.path;
                        });
                      },
                      child: Text("select"),
                    ),
                    ElevatedButton(
                      onPressed: () async {
                        print("kakabala");
                        dynamic resultValue =
                            await platform.invokeMethod("convert", filename);

                        // print(resultValue);
                        setState(() {
                          _imgPath = File(resultValue);
                        });
                      },
                      child: Text("convert"),
                    )
                  ],
                ))
              ],
            ),
          )),
    );
  }

  Widget _ImageView(imgpath) {
    if (imgpath == null) {
      return Center(
        child: Text("plz choose an image"),
      );
    } else {
      return Image.file(imgpath);
    }
  }
}
