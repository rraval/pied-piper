import wave
import math
import peakutils
import numpy as np
import peakutils.plot
import scipy.signal as signal
import matplotlib.pyplot as plt
from pprint import pprint as pp



def filter_frequencies(inp):
  wr = wave.open(inp, 'r')
  par = list(wr.getparams())
  par[3] = 0
  ww = wave.open('output.wav', 'w')
  ww.setparams(tuple(par))

  lowpass = 900
  highpass = 9000

  sz = wr.getframerate()
  c = int(wr.getnframes()/sz)

  for num in range(c):
    data = np.fromstring(wr.readframes(sz), dtype=np.int16)
    left, right = data[0::2], data[1::2]
    lf, rf = np.fft.rfft(left), np.fft.rfft(right)
    lf[lf < lowpass], rf[rf < lowpass] = 0, 0
    lf[lf > highpass], rf[rf > highpass] = 0, 0
    nl, nr = np.fft.irfft(lf), np.fft.irfft(rf)
    ns = np.column_stack((nl,nr)).ravel().astype(np.int16)
    ww.writeframes(ns.tostring())

  wr.close()
  ww.close()

  plot_freq(inp.split('.')[0])
  plot_freq('output')



def plot_freq(fname):
  wr = wave.open('%s.wav' % fname, 'r')
  sz = wr.getframerate()
  da = np.fromstring(wr.readframes(sz), dtype=np.int16)
  left, right = da[0::2], da[1::2]
  lf, rf = np.fft.rfft(left), np.fft.rfft(right)

  plt.figure(1)
  a = plt.subplot(211)
  r = 2**16/2
  a.set_ylim([-r, r])
  a.set_xlabel('time [s]')
  a.set_ylabel('sample value [-]')
  x = np.arange(44100)/44100

  plt.plot(x, left)
  b = plt.subplot(212)
  b.set_xscale('log')
  b.set_xlabel('frequency [Hz]')
  b.set_ylabel('|amplitude|')
  plt.plot(abs(lf))
  plt.savefig('%s.png' % fname)
  plt.clf()



## STEP 1
# filter_frequencies('capture2.wav')



def process_peaks(indexes, data):
  indexes = filter(lambda x: x > 1000, indexes)
  count = len(data)
  results = []

  for i in indexes:
    results.append(find_amplitude(i, data, count))

  return results


def find_amplitude(index, data, count):
  err = 0.02
  min_thres = index*(1.0-err)
  max_thres = index*(1.0+err)

  matches = []

  for i in data:
    cc = math.sqrt(i.real*i.real + i.imag*i.imag)
    if min_thres <= cc <= max_thres:
      matches.append(cc)

  matches = 20*np.log10(np.abs(np.array(matches))) # freq to db formula

  # f = np.linspace(0, 44100/2.0, count)
  # print len(f)
  # print len(matches)
  # plt.plot(f, matches)
  # plt.xlabel("Frequency(Hz)")
  # plt.ylabel("Power(dB)")
  # plt.show()
  # matches = map(lambda x: (x, x/count), matches)
  # 20*log10(abs(fft(wavefile)))
  # return [index, np.average(matches)]

  return np.average(matches)


def get_dominant_frequency(inp):
  wr = wave.open(inp, 'r')
  frame_rate = wr.getframerate()
  frame_count = wr.getnframes()
  clip_length = int(frame_count/frame_rate)

  sequence = []

  for i in range(clip_length):
    data = np.fromstring(wr.readframes(frame_rate), dtype=np.int16)
    left, right = data[0::2], data[1::2]
    lf, rf = np.fft.rfft(left), np.fft.rfft(right)
    rf = map(lambda x: x*-1.0, lf)

    indexes = peakutils.indexes(rf)
    sequence.append(process_peaks(indexes, rf))

  return sequence

f = get_dominant_frequency('test2.wav')

pp(f)

