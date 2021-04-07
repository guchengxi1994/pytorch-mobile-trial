import 'package:flutter/material.dart';

import 'MainPage.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'default',
      theme: defaultTheme,
      home: MainPage(),
    );
  }
}

final ThemeData defaultTheme = ThemeData(primaryColor: Colors.lightBlue);
