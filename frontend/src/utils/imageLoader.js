// Utility để load hình ảnh với fallback
export const loadImage = async (imagePath) => {
  try {
    // Với Vite, sử dụng dynamic import
    const module = await import(/* @vite-ignore */ imagePath);
    return module.default || module;
  } catch (error) {
    console.warn(`Không thể load hình ảnh: ${imagePath}`);
    return null;
  }
};

// Hoặc sử dụng cách đơn giản hơn với public folder
export const getImageUrl = (imageName) => {
  // Nếu hình ảnh trong public folder
  return `/images/${imageName}`;
};

