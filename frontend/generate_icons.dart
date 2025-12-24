import 'dart:io';
import 'dart:typed_data';

void main() {
  // Create simple colored PNG icons
  // This creates minimal valid PNG files
  
  // Favicon (16x16)
  final favicon = createSimplePNG(16, 16);
  File('web/favicon.png').writeAsBytesSync(favicon);
  print('Created web/favicon.png');
  
  // Icon-192
  final icon192 = createSimplePNG(192, 192);
  File('web/icons/Icon-192.png').writeAsBytesSync(icon192);
  print('Created web/icons/Icon-192.png');
  
  // Icon-512
  final icon512 = createSimplePNG(512, 512);
  File('web/icons/Icon-512.png').writeAsBytesSync(icon512);
  print('Created web/icons/Icon-512.png');
}

Uint8List createSimplePNG(int width, int height) {
  // Minimal PNG file structure
  // PNG signature
  final signature = [137, 80, 78, 71, 13, 10, 26, 10];
  
  // IHDR chunk (image header)
  final ihdr = [
    0, 0, 0, 13, // Length
    73, 72, 68, 82, // "IHDR"
    ...intToBytes(width, 4),
    ...intToBytes(height, 4),
    8, // Bit depth
    2, // Color type (RGB)
    0, 0, 0, // Compression, filter, interlace
  ];
  ihdr.addAll(intToBytes(crc32(ihdr.sublist(4)), 4));
  
  // IDAT chunk (image data) - solid teal color
  final imageData = <int>[];
  for (int y = 0; y < height; y++) {
    imageData.add(0); // Filter type
    for (int x = 0; x < width; x++) {
      imageData.addAll([0, 128, 128]); // Teal RGB
    }
  }
  
  final compressed = deflate(imageData);
  final idat = [
    ...intToBytes(compressed.length, 4),
    73, 68, 65, 84, // "IDAT"
    ...compressed,
  ];
  idat.addAll(intToBytes(crc32(idat.sublist(4)), 4));
  
  // IEND chunk
  final iend = [0, 0, 0, 0, 73, 69, 78, 68, 174, 66, 96, 130];
  
  return Uint8List.fromList([...signature, ...ihdr, ...idat, ...iend]);
}

List<int> intToBytes(int value, int length) {
  final bytes = <int>[];
  for (int i = length - 1; i >= 0; i--) {
    bytes.add((value >> (i * 8)) & 0xFF);
  }
  return bytes;
}

int crc32(List<int> data) {
  int crc = 0xFFFFFFFF;
  for (var byte in data) {
    crc ^= byte;
    for (int i = 0; i < 8; i++) {
      if (crc & 1 != 0) {
        crc = (crc >> 1) ^ 0xEDB88320;
      } else {
        crc = crc >> 1;
      }
    }
  }
  return crc ^ 0xFFFFFFFF;
}

List<int> deflate(List<int> data) {
  // Simplified deflate - just store uncompressed
  final result = <int>[120, 1]; // zlib header
  final chunks = <int>[];
  
  for (int i = 0; i < data.length; i += 65535) {
    final end = (i + 65535 < data.length) ? i + 65535 : data.length;
    final chunk = data.sublist(i, end);
    final len = chunk.length;
    final isLast = end == data.length ? 1 : 0;
    
    chunks.add(isLast);
    chunks.addAll(intToBytes(len, 2).reversed);
    chunks.addAll(intToBytes(~len & 0xFFFF, 2).reversed);
    chunks.addAll(chunk);
  }
  
  result.addAll(chunks);
  result.addAll(intToBytes(adler32(data), 4));
  return result;
}

int adler32(List<int> data) {
  int a = 1, b = 0;
  for (var byte in data) {
    a = (a + byte) % 65521;
    b = (b + a) % 65521;
  }
  return (b << 16) | a;
}
