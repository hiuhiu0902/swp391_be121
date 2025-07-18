const fetchBlogs = async (
    currentPage: number,
    isLoadMore = false,
    selectedCategory: string = "__"
) => {
    isLoadMore ? setLoadingMore(true) : setLoadingInitial(true);

    try {
        // Xây dựng params API
        const params: any = {
            page: currentPage,
            size: 10
        };

        // Chỉ gửi category khi không phải default
        if (selectedCategory && selectedCategory !== "__") {
            params.category = selectedCategory;
        }

        const res = await axios.get("http://localhost:8082/api/blogs/feed", {
            headers: { Authorization: `Bearer ${token}` },
            params
        });

        // Xử lý response data
        const newBlogs = Array.isArray(res.data) ? res.data : (res.data.content || []);

        // Update blogs state
        if (isLoadMore) {
            setBlogs((prev: Blog[]) => {
                // Loại bỏ trùng lặp bằng ID
                const existingIds = new Set(prev.map((blog: Blog) => blog.id));
                const uniqueNewBlogs = newBlogs.filter((blog: Blog) => !existingIds.has(blog.id));
                return [...prev, ...uniqueNewBlogs];
            });
        } else {
            setBlogs(newBlogs);
        }

        // Cập nhật hasMore
        setHasMore(newBlogs.length === 10);
    } catch (err) {
        console.error("Failed to load blogs:", err);
        setHasMore(false);
    } finally {
        setLoadingInitial(false);
        setLoadingMore(false);
    }
};

// Reset và fetch lại khi category thay đổi
useEffect(() => {
    setBlogs([]); // Clear blogs
    setPage(0);   // Reset page
    setHasMore(true); // Reset hasMore
    fetchBlogs(0, false, category);
}, [category]);

// Fetch thêm data khi page thay đổi
useEffect(() => {
    if (page > 0 && hasMore) {
        fetchBlogs(page, true, category);
    }
}, [page]);
