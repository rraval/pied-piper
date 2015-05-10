Pied Piper
==========

Transfer data between any two devices using nothing but a speaker and a
microphone (i.e. data transfer over sound waves).

Built during the VeloCity Residence Hacknight for Spring 2015 by Ronuk Raval and
Kartik Talwar.

Demo
----

Watch the [video on YouTube](http://youtu.be/fbW_YWu_HXI).

How it works
------------

The flow of data is unidirectional, going from a client (a tone generator over
a speaker) to a server (which is sampling a microphone). To transfer data, the
client encodes bits as single frequency sine waves emitted for a fixed amount of
time. The sine waves are then captured by the server, isolated, decoded, error
corrected, and finally the original data is reconstructed.

The following parameters must be agreed upon by both the server and client
ahead of time:

- The sampling interval: the duration of time which a single tone is held
  for. Empirical testing indicates that 100ms is reliable enough but 50ms
  runs into problems.

- Chunk size: the number of bits each tone represents. We use 4 for reliable
  transmission and 8 works most of the time.

- Handshake start frequency: indicates the start of a chunk sequence.
  8192 Hz for now.

- Handshake end frequency: no more chunks follow. 8704 Hz for now.

- Transmission start and step frequency: specify the frequency that should
  be generated for a given number. The full formula is:

  ```
  freq = start + (i * step)
  ```

  For 4 bit chunks (reliable comms), start = 1024 Hz and step = 256 Hz. For
  8 bit chunks, start = 1024 Hz and step = 16 Hz. These values essentially
  ensure that any data frequency lies below the handshake spectrum:

  ```
  (start + (2 ^ chunk_bits - 1) * step) < 8192
  ```

- Reed Solomon error bytes for forward error correction. Currently 4 bytes.

For a client to transmit data:

- Compute the Reed Solomon encoding for its payload.

- Emit the handshake start frequency

- Split its payload into bit size chunks of the right size. Convert and emit
  the frequencies corresponding to those chunks.

- Emit the handshake end frequency.

The server analyzes intervals half the size of the one the client uses (i.e.
twice as many times). This eliminates issues caused by the temporal phase shift
between the client generating tones and the server listening for them. For each
half interval:

- Compute the FFT and find the dominant frequency.

- If its the handshake start, start collecting frequency data.

- If its handshake end, stop collecting frequency data and decode:

    - Throw away every alternate frequency found (twice as many samples!)

    - Filter away frequencies not in the right range

    - Convert remaining frequencies into their representative bit chunks,
      combining that into a byte stream.

    - Validate and correct byte stream with Reed Solomon

Performance
-----------

The current best and reasonably reliable parameters involve 8 bits per tone and
10 tones per second. This translates to 10 bytes per second.

The data transfer itself is mostly unaffected by background human chatter.

Further Work
------------

- It should be quite possible to transmit multiple frequencies per tone instead
  of just one. The server would then look to decompose that many frequencies
  instead of just the most dominant one.

  This could easily double or quadruple throughput. Some work has been done on
  the [multifreq branch](https://github.com/rraval/pied-piper/tree/multifreq)
  but we ran out of time for the hackathon.

- Experiment to find the range of frequencies that can reliably be transmitted
  and recorded. A wider range means more chunk bits, which directly impacts
  throughput.

- Reed Solomon error correction may not be the ideal one to use here.
  Investigate alternatives that might provide better performance.

- The Reed Solomon libraries only support combined payload + error byte sizes of
  255 bytes. Larger payloads would need to be chunked across several error
  corrected packets, which requires client and server support for it.
