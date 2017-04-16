#!/usr/bin/env python 
from getfp.wttune.dejavu import Dejavu
from getfp.wttune import dejavu_1 as dejvu
from getfp.wttune.dejavu.recognize import MicrophoneRecognizer,FileRecognizer
import os

class user_inp:


      def __init__(self):
          config = {
             "database": {
             "host": "ec2-54-70-182-249.us-west-2.compute.amazonaws.com",
             "user": "wtt",
             "passwd": "wtt123",
             "db": "clusterdb",
             },
             "database_type" : "mysql",
             "fingerprint_limit" : 10
          }
          self.djv = Dejavu(config)


      def search_tune_mic(self):
          song = self.djv.recognize(MicrophoneRecognizer, seconds=10)
          #ong = dejvu.main('--recognize mic 10')
          print(song['song_name'])
          print(song)
          #return song['song_name']
          return song

 
      def search_tune_wav(self,audiofile):
          try: 
              song = self.djv.recognize(FileRecognizer, audiofile)
              return song
          except Exception as e:
              print(str(e))
          else:
              print("No Valid matching metadata found for provided audio sample")


      def make_fingerprint(self,audiofile):
          try:
              self.djv.fingerprint_file(audiofile)
              return "Successful in uploading to Acoustic Server"
          except Exception as e:
              print(str(e))
          else:
              return "Failed to Upload to Acoustic Server"
