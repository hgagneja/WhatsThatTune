from django.shortcuts import render
from django.views.generic import TemplateView
from getfp.wtt import user_inp
from django.views.decorators.csrf import csrf_exempt
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.http import Http404
import os


class AudioSample_upload(APIView):
    """
    To get the meta data of audio sample
    """
    #parser_classes = (FileUploadParser,)
    def get(self):
        content = {'Acoustic Fingeprint server': 'nothing to see here'}
        return Response(content, status=status.HTTP_200_OK)

    def post(self, request, format='wav'):
        audio_sample_file = request.FILES['file']
        filepath = os.getcwd()
        #destination = open(filepath + '/getfp/input/' + audio_sample_file.name, 'wb+')
        with open(filepath + '/getfp/input/' + audio_sample_file.name, 'wb+') as destination:
            for chunk in audio_sample_file.chunks():
                destination.write(chunk)
        
        song_name = ''
        song_matchtime = ''
        song_artist = ''
        song_album = ''
        inp = user_inp()
        audio_filename = filepath + '/getfp/input/' + audio_sample_file.name
        song = inp.search_tune_wav(audio_filename)
        if song:
                song_artist,song_name,song_album = song['song_name'].split("--")
                confidence = song['confidence']
                song_list = [song_album,song_name,song_artist,confidence]

                return Response(song_list,status.HTTP_200_OK)
        else:
                song_list = ["No Match Found"]
                return Response(song_list,status.HTTP_200_OK)


class Audio_upload(APIView):
    """
    To upload audio file and create fingerprint
    """
    def get(self):
        content = {'Acoustic Fingeprint server': 'nothing to see here'}
        return Response(content, status=status.HTTP_200_OK)

    def post(self, request, format='mp3'):
        audio_sample_file = request.FILES['file']
        filepath = os.getcwd()
        #destination = open(filepath + '/getfp/input/' + audio_sample_file.name, 'wb+')
        with open(filepath + '/getfp/input_fp/' + audio_sample_file.name, 'wb+') as destination:
            for chunk in audio_sample_file.chunks():
                destination.write(chunk)

        inp = user_inp()
        audio_filename = filepath + '/getfp/input_fp/' + audio_sample_file.name
        st = inp.make_fingerprint(audio_filename)
        

        return Response(st,status.HTTP_200_OK)



# Create your views here.
class HomePage(TemplateView):
    def get(self, request, **kwargs):
        return render(request, 'main.html', context=None)

    @csrf_exempt 
    def post(self, request, **kwargs):
        song_name = ''
        song_matchtime = ''
        song_artist = ''
        song_album = ''
        usr_inp = wtt.user_inp()
        song = usr_inp.search_tune_mic()
        song_album,song_name = song['song_name'].split("--")
        song_artist = song_album
        return render(request, 'index.html', {'message_name': song_name,'message_artist' : song_artist, 'message_album': song_album } )

class ResultPage(TemplateView):
    def get(self, request, **kwargs):
        song_name = ''
        song_matchtime = ''
        song_artist = ''
        song_album = ''
        usr_inp = wtt.user_inp()
        song = usr_inp.search_tune_mic()
        song_album,song_name = song['song_name'].split("--")
        song_artist = song_album
        return render(request, 'index.html', {'message_name': song_name,'message_artist' : song_artist, 'message_album': song_album } )


class AboutPage(TemplateView):
    template_name = "about.html"


class ServicesPage(TemplateView):
    template_name = "services.html"



class ContactPage(TemplateView):
    template_name = "contact.html"
